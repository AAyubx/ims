# Barcode Management Requirements (Inventory Management System)
**Document Version:** v1.0  
**Context:** Complements the Item Master & Catalog plan (see Week 4: Barcode & Media). This document details comprehensive, modern barcode management for a multi‑tenant inventory system and explicitly **links barcode creation with item & variant creation**.  
**Applies To:** Web + Mobile (scanning/ops), APIs, DB schema, printing services.

---

## 1. Goals & Non‑Goals
**Goals**
- Provide a robust, GS1‑aware, tenant‑scoped barcode capability for **variant‑level identification**, **pack hierarchies** (each/inner/case/pallet), and **UoM‑specific barcodes**.
- Ensure **global uniqueness per tenant**, **fast lookup**, **strong validation** (format + check digit), and **first‑class UX** for creation, import, search, and scanning.
- Align with **item lifecycle**: activation gates, duplication prevention, and auditability.

**Non‑Goals**
- Full serialization/track‑and‑trace (lot/serial) policies (integration hooks provided).
- Artwork design tooling beyond label templates (use external DLS/labeling solutions).

---

## 2. Scope & Roles
**In-scope entities:** `ItemVariant` (SKU), `ItemBarcode`, `UnitOfMeasure`, optional `PackLevel` (EACH, INNER, CASE, PALLET), `LabelTemplate` definitions.  
**Roles:** Tenant Admin, Catalog Manager, Purchasing, Warehouse Ops, Store Ops (scan-only), API Client (system).  
**Surfaces:** Admin portal (web), Ops mobile app (scan/lookup), REST APIs, background workers, printing microservice.

---

## 3. Supported Symbologies & Standards
**Linear (1D)**
- **UPC-A** (GTIN‑12), **UPC‑E** (compressed UPC), **EAN‑13** (GTIN‑13), **EAN‑8** (GTIN‑8)
- **ITF‑14** (GTIN‑14; corrugated / case level)
- **GS1‑128** (formerly UCC/EAN‑128) for application identifiers (AIs: lot, exp date, weight)
- **Code 128** (general purpose), **Code 39** (legacy compatibility)

**2D**
- **DataMatrix (GS1)**, **QR (GS1 Digital Link)** for compact AIs, digital link URLs

**Standards Awareness**
- **GS1 Company Prefix** support for tenants using official GTIN allocation (import of prefix + capacity planning)
- **Check digit** computation (Mod‑10) for GTINs; AI syntax validation for GS1‑128/DataMatrix/QR
- **Application Identifiers (AIs)** parsing/storage for encoded attributes (lot 10, serial 21, exp 17, net weight 310x, etc.)

---

## 4. Data Model Requirements
- **ItemBarcode**
  - `id`, `tenant_id`, `variant_id`, `barcode` (string 4..64), `barcode_type` (enum: UPC_A, UPC_E, EAN_13, EAN_8, ITF_14, GS1_128, CODE_128, CODE_39, DATAMATRIX_GS1, QR_GS1_LINK),
  - `uom_id` (nullable), `pack_level` (enum: EACH, INNER, CASE, PALLET, null), `is_primary` (bool), `status` (enum: RESERVED, ACTIVE, DEPRECATED, BLOCKED),
  - `ai_payload` (JSON, optional; parsed AIs), `label_template_id` (nullable), `created_by`, `created_at`, `updated_at`.
- **Uniqueness constraints**
  - `(tenant_id, barcode)` **unique** across all variants.
  - Only **one primary barcode** per `(tenant_id, variant_id)` per **UoM/pack_level** combination.
- **Indexes**
  - `IDX (tenant_id, barcode)`; `IDX (tenant_id, variant_id)`; `IDX (tenant_id, is_primary)`.
- **Soft delete** (prefer `status=DEPRECATED` instead). Hard delete only when no references (labels, shipments, orders).

---

## 5. Lifecycle & Link to Item Creation
**5.1 ItemWizard/VariantMatrix Integration**
- **Step: UoM & Barcodes** in item creation flow:
  1) Choose **base UoM** (e.g., EACH).  
  2) For each **variant** generated (e.g., Color/Size), set or **auto‑generate** a **primary barcode** per base UoM.  
  3) Optional: define **inner/case/pallet** pack levels with **UoM conversions** and assign barcodes (ITF‑14 recommended for CASE).

- **Auto‑generate policy** (configurable per tenant):
  - **GTIN allocation** from tenant prefix (UPC/EAN/ITF‑14) — maintain a **sequence** with reserved/consumed ranges.
  - **Non‑GTIN** fallback using Code 128 (internal scheme), clearly marked as **non‑retail** (cannot be used at POS if policy requires GTIN).

- **Activation Gate** (Item or Variant → ACTIVE):
  - If `requirePrimaryBarcodeOnActivation = true`: must have **exactly one** primary barcode per variant (for base UoM).  
  - Validate barcode format, uniqueness, and if GTIN: **check digit** + **prefix range**.  
  - If CASE/PALLET defined, verify UoM conversions exist and are valid.

**5.2 Status Transitions**
- `RESERVED → ACTIVE` on successful item activation or explicit assignment.  
- `ACTIVE → DEPRECATED` when barcode replaced/retired; system keeps historical mapping for search & order history.  
- `BLOCKED` for known counterfeit/erroneous numbers; prevents creation/use.

---

## 6. Validation & Business Rules
**Format & Algorithmic**
- Trim spaces; reject non‑printable characters; NFC normalize.  
- GTIN length rules (8/12/13/14) + **Mod‑10 check digit**.  
- **UPC‑E ↔ UPC‑A** expansion rules; **ITF‑14** start/stop/quiet zones (printer guidance).  
- GS1‑128/DataMatrix/QR: validate AI syntax; **(01)** GTIN presence if policy enforces.

**Uniqueness & Conflicts**
- Tenant‑global uniqueness for `barcode`.  
- Prevent multiple primaries per variant (by UoM/pack_level).  
- Detect look‑alike collisions (O vs 0, I vs 1) if policy enabled (soft warning).

**Pack/UoM Consistency**
- If `pack_level ≠ EACH`, require `uom_id` and valid **conversion factor** from base UoM.  
- **ITF‑14** recommended for `CASE`/`PALLET`; forbid EAN‑8/UPC‑E at case level.

**Security & Permissions**
- Only roles with `catalog.barcode.manage` can create/update/delete; scan/lookup allowed to ops roles.  
- Editing **primary** requires `If‑Match` (ETag) and audit reason.

**Imports**
- CSV/Excel import with column mapping: `variant_sku`, `barcode`, `type`, `uom_code`, `pack_level`, `is_primary`.  
- Dry‑run with validation report; upsert by `(tenant_id, barcode)` or `(tenant_id, variant_sku + pack_level)`.  
- Reject duplicates; partial commits allowed only in **best‑effort** mode.

---

## 7. APIs
**REST (tenant‑scoped)**
- `GET /catalog/barcodes?barcode=...|sku=...|variantId=...` — search/lookup  
- `POST /catalog/variants/{variantId}/barcodes` — create (supports `is_primary`, `pack_level`, `uomId`)  
- `PATCH /catalog/barcodes/{id}` — update (toggle primary, change status, assign template)  
- `DELETE /catalog/barcodes/{id}` — deprecate or hard delete if allowed  
- **Bulk:** `POST /catalog/barcodes/import` (async job), `GET /catalog/barcodes/export`

**Webhooks (events)**
- `catalog.barcode.created|updated|primary_changed|deprecated|deleted` with payload: `tenantId`, `variantId`, `uomId`, `packLevel`, `barcode`, `type`, `status`, `actor`, `timestamp`.

**Idempotency & Concurrency**
- Require `Idempotency-Key` on POST; `If‑Match` on PATCH/DELETE; return `409 Conflict` on ETag mismatch.

---

## 8. UI/UX Requirements
**Admin (Web)**
- **Barcode Manager** panel on Variant page: list (type, code, UoM, pack, primary, status), add/edit/delete.  
- **Generator Drawer**: choose type (UPC/EAN/ITF‑14/Code128/GS1‑128/2D), **auto‑generate** or **manual entry** with live validation & **check digit** preview.  
- **Primary Switch**: radio button enforcing one primary per UoM/pack.  
- **Duplicate Detector**: instant feedback if code exists in tenant; link to owning variant.

**ItemWizard Integration**
- Step shows **pending variants** and indicates which need barcodes.  
- “Generate barcodes for all variants” bulk action; supports **GTIN from prefix** or **internal Code 128**.  
- Case/pallet section appears if UoM conversions defined; suggests **ITF‑14**.

**Ops (Mobile)**
- **Scan to lookup**: camera (ML Kit / ZXing) with symbology toggles; offline cache recent lookups.  
- **Haptic + Toast** on match; show variant, UoM, stock availability (if inventory module).  
- **Error states**: unknown barcode (offer create flow if role permits).

**Accessibility & Internationalization**
- Large input fields; copy‑to‑clipboard; RTL‑safe. Tooltips explaining GTIN / AI concepts.

---

## 9. Printing & Labeling
- Integrate with a **Label Service**:
  - Templates: ZPL/EPL/PDF; parameters: `barcode`, `type`, `name`, `sku`, `price`, `size`, etc.
  - Endpoints: `POST /labels/print` with `templateId`, `data[]`, `printerId`.
- Preview: client‑side SVG/PNG render for supported symbologies.  
- Batch printing: from search results, from import job results, or from receiving (future).

---

## 10. Performance & Observability
- **Lookup P95 < 50 ms**, creation/update P95 < 200 ms.  
- DB indexes per Section 4; Redis cache for hot barcodes.  
- Metrics: `barcode.lookup.count`, `duplicate.detected.count`, `primary.switch.count`, `validation.fail.count`.  
- Audit every create/update/delete with before/after.

---

## 11. Error Catalogue (API/UX)
- `barcode_unique` — code already exists in tenant.  
- `barcode_invalid_checksum` — GTIN check digit mismatch.  
- `type_not_allowed_for_pack_level` — e.g., EAN‑8 for CASE.  
- `primary_already_exists` — attempting second primary for same UoM/pack.  
- `uom_required_for_pack_level` — INNER/CASE/PALLET without UoM.  
- `ai_syntax_invalid` — malformed GS1 AIs.  
- `prefix_capacity_exhausted` — no GTINs left in tenant prefix pool.

**Error Envelope**
```json
{
  "status": 422,
  "error": "ValidationError",
  "traceId": "req_abc",
  "errors": [
    {"field": "barcode", "code": "barcode_invalid_checksum", "message": "Check digit does not match"},
    {"field": "packLevel", "code": "uom_required_for_pack_level", "message": "Define a UoM for CASE level"}
  ]
}
```

---

## 12. Security & Compliance
- RBAC per action; restrict export of full barcode lists to Admin/Manager.  
- Optional **checksum masking** in UI to prevent shoulder‑surfing.  
- PII‑free by default; treat as **sensitive operational data** for audit & retention.

---

## 13. Acceptance Criteria (Gherkin)

```gherkin
Feature: Primary barcode uniqueness per variant
  Scenario: Enforce one primary per variant/UoM
    Given a variant "V123" has a primary EAN-13 for UoM EACH
    When I set another barcode as primary for UoM EACH
    Then the API responds 409 with code "primary_already_exists"
```

```gherkin
Feature: GTIN checksum validation
  Scenario: Reject invalid check digit
    Given I input EAN-13 "4006381333932"
    And the check digit computed is 1
    When I submit the barcode
    Then I receive 422 "barcode_invalid_checksum"
```

```gherkin
Feature: Duplicate detection across tenant
  Scenario: Prevent barcode reuse
    Given barcode "6291234567890" exists for variant "VX"
    When I assign "6291234567890" to another variant
    Then the API returns 409 "barcode_unique"
```

```gherkin
Feature: Auto-generate GTIN from tenant prefix
  Scenario: Generate next GTIN-13
    Given tenant T1 has GS1 prefix "6291234" with capacity remaining
    When I click "Auto-generate" for 10 variants
    Then the system reserves and assigns 10 sequential GTIN-13 with valid check digits
```

```gherkin
Feature: Case-level barcode requires UoM and conversion
  Scenario: ITF-14 for CASE
    Given a variant has base UoM EACH and CASE conversion 12 EACH
    When I add an ITF-14 barcode for CASE
    Then the operation succeeds
    And lookup resolves CASE → EACH using conversion
```

---

## 14. Implementation Notes & Flags
- Feature flags: `barcode.autoGenerate.enabled`, `barcode.gs1Ai.requiredForGs1Symbologies`, `barcode.dupSoftWarning.enabled`.
- Reuse existing CRUD pipelines: **Pre‑validate → Normalize → Authorize → Persist → Index/Cache → Audit → Events**.
- Search indexing: include `(barcode, type, uom, pack_level, is_primary, status)` for variant search faceting.

---

## 15. Deliverables Checklist
- DB migrations for `item_barcode` enhancements + indexes.
- Backend services: validation, generator (GTIN + check digit), AI parser.
- REST endpoints + OpenAPI with examples and error codes.
- Web UI: Variant page + Wizard step + Manager panel + bulk operations.
- Mobile: scan/lookup screen with camera integration, offline cache.
- Printing service integration with templates and batch API.
- Unit/integration tests; E2E scenarios from Section 13.
