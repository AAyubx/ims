
# Inventory Item Master — Must‑Haves & Retail Classification (Merged)

> This document merges **item-master-must-haves.md** and **item-classification-retail.md** into one reference.

- [Item Master / Item Creation — Must‑Have Features](#item-master--item-creation--musthave-features-modern-inventory-system)
- [Item Classification & Hierarchy (Retail)](#item-classification--hierarchy-retail--linkages-to-item-master--downstream-modules)

---


# Item Master / Item Creation — Must‑Have Features (Modern Inventory System)

This checklist focuses on **UI** (what users see/do) and **UX** (how it behaves/helps), with detailed descriptions and purposes.

---

## 1) Core Identification
**UI:** Fields for **Item ID** (system), **SKU/UPC/EAN**, **Item Name**, **Short Name**, **Category**, **Sub‑category**, **Brand**.  
**UX:** Auto‑generated IDs; uniqueness validation; real‑time duplicate warning on SKU/UPC; type‑ahead for categories/brands.  
**Purpose:** Ensure unique, searchable identity and consistent classification across modules.

---

## 2) Variants & Attributes (Parent/Child)
**UI:** Toggle for “This item has variants”; matrix editor for **Size/Color/Material**; attribute sets (custom key‑value pairs).  
**UX:** Create parent item + child SKUs in one flow; preview auto‑generated variant SKUs and barcodes; bulk edit variant attributes.  
**Purpose:** Efficiently model catalog families and reduce manual errors.

---

## 3) Units of Measure (UoM) & Conversions
**UI:** **Base UoM** (e.g., EA), **Alt UoMs** (BOX, CASE), conversion table (1 CASE = 24 EA), **Sell/Buy/Stock UoMs**.  
**UX:** Validation of conversion loops; guardrails against fractional quantities when disallowed; inline examples.  
**Purpose:** Accurate stock, purchasing, and sales calculations across units.

---

## 4) Barcodes & Identifiers
**UI:** Fields for **Primary Barcode**, **Additional Barcodes** (per UoM/variant), **GTIN/EAN/UPC**, supplier codes.  
**UX:** Barcode generator/validator; duplicate‑barcode detection across catalog.  
**Purpose:** Scannability at receiving, counting, POS, and fulfillment.

---

## 5) Lifecycle State & Publish Controls
**UI:** Status selector (**Draft, Active, Inactive, Discontinued**); **Publish** toggles per channel (POS, e‑com, marketplace).  
**UX:** State‑based validation (e.g., must have price before Active); bulk publish/unpublish; scheduled go‑live.  
**Purpose:** Govern visibility; prevent incomplete items from leaking into operations.

---

## 6) Pricing & Costing
**UI:** **Standard Cost**, **Last Cost**, **Average Cost** (read‑only if system‑calculated); **Base Price**; channel/region price lists; **Tax class**.  
**UX:** Currency‑aware inputs; price‑list inheritance and overrides; validation (min/max margins).  
**Purpose:** Consistent pricing and margin control across sales channels and currencies.

---

## 7) Inventory Policies
**UI:** **Safety Stock**, **Reorder Point**, **Reorder Qty** (fixed/min‑max); **Lot/Serial Tracking** toggle; **Shelf‑life/Expiry** settings; **FEFO/FIFO**.  
**UX:** Inline guardrails (e.g., REORDER POINT ≥ SAFETY STOCK); show forecast/ATP preview; enable lot attributes when toggled.  
**Purpose:** Accurate replenishment and compliant stock handling.

---

## 8) Locations & Availability
**UI:** Multi‑select of **Sites/Stores/Warehouses** with per‑site overrides (status, price, min/max, backorder rules).  
**UX:** “Apply to all” + per‑site exceptions; import/export grid; visibility chips (available online/in‑store).  
**Purpose:** Control where the item exists and how it behaves by location.

---

## 9) Compliance & Content
**UI:** **HS Code**, **Country of Origin**, **Regulatory flags** (hazmat, age restriction), **Care/ingredient** fields; **Images** (primary/alt), **Docs** (MSDS, spec sheets).  
**UX:** Drag‑drop media; automatic image optimization; required fields based on category/regulatory flags.  
**Purpose:** Legal compliance and rich content for operations and channels.

---

## 10) SEO & Channel Metadata
**UI:** **Web Title**, **Meta Description**, **Slug**, **Search Keywords**; per‑channel attribute mapping.  
**UX:** Slug auto‑generation with manual override; character counts; preview snippets.  
**Purpose:** Improve searchability and consistent syndication.

---

## 11) Supplier & Procurement
**UI:** **Preferred Supplier**, **Supplier Item Code**, **MOQ**, **Lead Time**, **Contract Price**, **Incoterms**.  
**UX:** Alerts when MOQ/lead time conflict with min‑max; show landed‑cost preview.  
**Purpose:** Streamlined purchasing and accurate delivery expectations.

---

## 12) Kitting/Bundles/BOM
**UI:** Kit/BOM builder to add components with quantities; pricing strategy (sum of parts/override).  
**UX:** Stock policy for kits (assembled‑to‑order vs. pre‑built); component availability check.  
**Purpose:** Support bundles and manufacturing‑light scenarios.

---

## 13) Audit & Governance
**UI:** Read‑only **Created/Updated by/at**, **Change history**, **Approval workflow** (submit/review/approve).  
**UX:** Role‑based permissions; annotation on fields (“why changed”); diff viewer for audits.  
**Purpose:** Traceability and control over catalog quality.

---

## 14) Accessibility & Internationalization
**UI:** WCAG‑compliant inputs, labels, and error states; field tooltips; language switch for translatable fields.  
**UX:** Keyboard navigation, visible focus, screen‑reader support; placeholder examples localized.  
**Purpose:** Inclusive design and global usability.

---

## 15) Performance, Drafts & Collaboration
**UI:** Auto‑save drafts; “Ready for review” flag; comment thread per item.  
**UX:** Presence indicators (who’s editing); conflict resolution; fast, virtualized variant tables.  
**Purpose:** Reduce data loss; enable teamwork without stepping on each other.

---

## 16) Validation & Guardrails (System)
- Required fields by item type/category/state.  
- Referential integrity (categories, suppliers, UoM).  
- Margin floors, price conflicts, barcode uniqueness, conversion consistency.  
- Pre‑publish checks (images, tax class, price).

---

## 17) Import/Export & APIs
**UI:** CSV/Excel import with mapping templates; export current view or full item.  
**UX:** Validation report with row‑level errors; bulk fix and re‑import; API link to create/update items.  
**Purpose:** High‑volume onboarding and integrations.

---

## 18) Help & Onboarding
**UI:** Context help, show‑me tours, field explanations; glossary.  
**UX:** Empty‑state guidance (“Add your first variant”); quick actions; sample templates.  
**Purpose:** Reduce training time and errors for new users.


---


# Item Classification & Hierarchy (Retail) — Linkages to Item Master & Downstream Modules

This document extends **item-master-must-haves.md** by defining **classification structures**, **order of precedence**, and **relationships** between the Item Master and other modules. It also highlights **prerequisite development work** (metadata, reference data, validation, APIs).

---

## 1) Classification Goals

- Create a **consistent taxonomy** so items are searchable, reportable, and operable across channels.  
- Support **governance**: Who can create/modify Brands/Categories/Attributes? What validations apply?  
- Enable **automation**: defaults (tax, UoM, reorder policy), price-list mapping, channel enrichment, and analytics segmentation.

---

## 2) Core Objects & Order of Precedence

1. **Category** (taxonomy backbone) → `Department > Category > Subcategory`  
2. **Brand** (trade identity; may cut across categories)  
3. **Item (Parent)** (style/model)  
4. **Item (Variant/Child)** (size/color/material combinations)  
5. **Attributes** (global or category-specific; e.g., Color, Size, Gender, Season)  
6. **Supplier Linkages** (preferred supplier, supplier item code, MOQ, lead time)  
7. **Channel/Region Overlays** (price lists, publish flags, compliance, translations)

**Why this order?**  
- Category determines **default rules** (tax class, compliance flags, content requirements).  
- Brand enriches **merchandising** and channel facets but does **not** drive rules that categories govern.  
- Parent/Variant structure ensures clean **inventory and pricing** at the sellable SKU level.  
- Attributes are scoped by category to prevent attribute sprawl.

---

## 3) Retail Reference Hierarchy (Example)

```
Department
 └─ Category
    └─ Subcategory
       └─ Brand (orthogonal; one item has one brand; brand spans multiple categories)
          └─ Item (Parent/Style: e.g., AirMax 90)
             └─ Variants (Child SKUs: Color x Size)
```

- **Orthogonal** means Brand exists alongside the category tree and associates to items directly.  
- Some retailers optionally add **Collection/Season** (e.g., Spring 2026) between Brand and Item for planning/markdowns.

---

## 4) Classification Components & Module Linkages

| Component | Description | Precedence / Owner | Key Fields | Links to Modules | Notes |
|---|---|---|---|---|---|
| **Department** | Top-level retail division (e.g., Apparel, Footwear) | Highest taxonomy level; owned by Merch Ops | Code, Name, Active | Merch planning, Assortment, Reporting | Typically <50 values |
| **Category** | Business grouping under Department | Inherits Dept; owned by Merch Ops | Code, Name, Tax Class Default | Pricing, Tax, Compliance, Search facets | Drives defaults & validations |
| **Subcategory** | Finer grouping | Child of Category | Code, Name, Attribute Set | Item Master, Search, Analytics | Maps to attribute templates |
| **Brand** | Manufacturer/label/trademark | Orthogonal; owned by Brand/Marketing | Code, Name, Vendor Links | Marketing, Search facets, Pricing rules | May include brand-level content |
| **Item (Parent/Style)** | Non-sellable style/model | Under Subcategory; linked to Brand | Style Code, Name, Attribute Set | Item Master, PIM, SEO | Generates children |
| **Item (Variant/Child)** | Sellable SKU (size/color) | Child of Parent | SKU, Barcode(s), UoM | Inventory, POS, WMS, OMS | Stock & price live here |
| **Attribute** | Data points (Color/Size/Material) | Scoped by Category | Code, Type, Allowed Values | Item Master, Search, Filters | Avoid global free-text |
| **Supplier Link** | Preferred source data | Item ↔ Supplier | Supplier Item Code, MOQ, Lead Time | Procurement, Replenishment | Drives purchasing rules |
| **Channel/Region Overlay** | Per-channel visibility & price | Item ↔ Channel/Region | Price List, Publish, Tax | Ecommerce, Marketplace, POS | Overrides global defaults |
| **Compliance** | Regulatory data | Category-driven | HS Code, COO, Hazmat | Trade, Shipping, Customs | Validated on publish |
| **Media/Content** | Images & docs | Item/Brand level | Primary/Alt Images, MSDS | PIM, Ecommerce | Quality gates before publish |

---

## 5) Data Model (Simplified ER Diagram)

```
[Department] 1---n [Category] 1---n [Subcategory] 1---n [ItemParent] 1---n [ItemVariant]
      |                                                  |                |
      |                                                  |                n
      |                                                  |               [Barcode]
      |                                                  n
      |                                                 [ItemAttributeValue] --- [AttributeDefinition] --- [AttributeSet]
      |
      n
   [Brand] (orthogonal) ---1 n--- [ItemParent]

[ItemVariant] --- n 1 --- [Supplier]
[ItemVariant] --- n 1 --- [PriceListEntry] (by Channel/Region/Currency)
[ItemVariant] --- n 1 --- [InventoryByLocation]
[ItemVariant] --- n 1 --- [Compliance] (HS, COO, Flags)
```

**Notes**  
- **ItemParent** stores shared fields; **ItemVariant** stores sellable SKU data (barcode, price, stock).  
- **Brand** links to **ItemParent** (or directly to Variant if no Parent concept).  
- **AttributeDefinition** is grouped in **AttributeSet** and tied to **Subcategory** for relevance.

---

## 6) Validation & Guardrails (Prereq Dev Work)

- **Uniqueness:** `SKU`, `Barcode`, and `Style Code` must be unique within defined scopes.  
- **Required per Level:** Category/Subcategory required before Brand/Item.  
- **Attribute Templates:** Attach **AttributeSet** to Subcategory; variants must satisfy required attributes (e.g., Color, Size).  
- **Defaults by Taxonomy:** Tax class, compliance flags, and content requirements derive from Category/Subcategory.  
- **State Machine:** Disallow `Active` unless required fields (images, price, tax class, UoM) are present.  
- **Cross-links:** Require Supplier Link (or waiver) before enabling Procurement; require Channel Overlay before publish to that channel.  
- **APIs:** CRUD for Brand/Category/Attributes; validations on create/update; bulk import with row-level error report.

---

## 7) Retail Examples

### Example A — Footwear Sneaker
- **Department**: Footwear → **Category**: Sneakers → **Subcategory**: Running  
- **Brand**: AirMax Co.  
- **Parent Item**: AirMax 90 (Style)  
- **Variants**:  
  - SKU: AM90-BLK-42 | Color: Black | Size: 42 | Barcode: 1234567890123  
  - SKU: AM90-BLK-43 | Color: Black | Size: 43 | Barcode: 1234567890124  
- **Supplier Link**: GlobalSports Ltd. (Supplier Code AM90‑BLK, MOQ 20, Lead 14d)  
- **Price Overlay**: POS UAE AED 499; E‑com KSA SAR 549 (FX as of 2025‑09‑23)  
- **Compliance**: HS 6404; COO Vietnam  
- **Downstream**: InventoryByLocation tracks stock per store; PriceListEntry controls channel pricing; OMS uses barcode for picking.

### Example B — Cosmetics Lipstick
- **Department**: Beauty → **Category**: Cosmetics → **Subcategory**: Lipstick  
- **Brand**: LushHue  
- **Parent Item**: LushHue Velvet (Style)  
- **Variants**:  
  - SKU: LHV-RED | Shade: Crimson Red | Barcode: 9988776655443  
  - SKU: LHV-NUD | Shade: Nude | Barcode: 9988776655444  
- **Supplier Link**: GlamDistrib FZE (MOQ 50, Lead 10d)  
- **Price Overlay**: POS UAE AED 89; E‑com UAE AED 89; Marketplace discount allowed 10%  
- **Compliance**: Ingredient list doc; not hazmat  
- **Downstream**: PIM pushes images/content; POS uses tax class from Category; BI segments by Brand + Subcategory.

---

## 8) API & Import Considerations

- **Reference Data Endpoints**: `/categories`, `/brands`, `/attributesets` with versioning and soft-delete.  
- **Item Creation Flow**: Create Category/Subcategory → Brand → Parent Item → Variants → Supplier Links → Channel Overlays → Publish.  
- **Bulk Import**: CSV with separate tabs (Brands, Categories, Attributes, Items, Variants, Barcodes, SupplierLinks, Prices). Cross-sheet validation with row references.

---

## 9) Reporting & Analytics

- Sales and stock by **Department/Category/Subcategory/Brand**.  
- Markdown strategy by **Season/Collection** (optional dimension).  
- Fill-rate and lead-time adherence by **Supplier Link**.  
- Content/Compliance completeness (% items with images, HS codes, COO, tax class).

---

## 10) Governance & Ownership

- **Merchandising** owns Department/Category/Subcategory and default rules.  
- **Brand/Marketing** owns Brand definitions and brand media.  
- **Master Data** (MDM) governs Items/Attributes and approvals.  
- **Procurement** owns Supplier Links; **Pricing** owns price lists; **Compliance** owns regulatory data.

---

### Quick Matrix — Relationship Examples

| Relationship | Cardinality | Purpose |
|---|---|---|
| **ItemVariant ↔ Barcode** | 1–n | Multiple barcodes per SKU (UoM/region) |
| **ItemParent ↔ ItemVariant** | 1–n | Style → sellable SKUs |
| **ItemParent ↔ Brand** | n–1 | Brand identity for reporting/faceting |
| **Subcategory ↔ AttributeSet** | 1–n | Enforce relevant attributes |
| **ItemVariant ↔ Supplier** | n–1 | Preferred source, MOQ, lead time |
| **ItemVariant ↔ PriceListEntry** | n–n | Channel/region/currency pricing |
| **ItemVariant ↔ InventoryByLocation** | n–n | Per‑site stock controls |
| **ItemVariant ↔ Compliance** | 1–1 or 1–n | HS/COO/flags; region-specific |
| **Category ↔ TaxClass** | n–1 | Default tax rules |

---

## 11) Next Steps (Dev Backlog Seeds)

- Schema migrations for **Brand/Category/Subcategory/AttributeSet** tables and constraints.  
- Services: **Classification API**, **Validation Service**, **Import Service**, **Price Overlay Service**.  
- UI: **Guided item creation** wizard with taxonomy-first steps and attribute templates.  
- Tests: Contract tests for classification APIs; DST/locale regressions for overlays.

