# SaaS ERP (Inventory) — UI Design Guide

This guide consolidates modern UX patterns for SaaS ERP inventory systems and a recommended main application shell. It includes text wireframes you can share with stakeholders.

---

## 1) Main Application UI (App Shell + Default Landing)

### App shell (structure)
- **Left rail (collapsible):** Modules with icons + labels → *Inventory, Replenishment, Orders, Transfers, Catalog, Suppliers, Reports, Admin*. Include a **Starred** area for saved views.
- **Top bar:** **Global search / ⌘K**, breadcrumb, **environment chip** (Prod/UAT), Alerts, Help, User menu.
- **Content grid:** 12-column desktop; density toggle (Comfortable/Compact); sticky page title + actions.

### Default landing (“My Work”, not a vanity dashboard)
- **KPI strip (4–5 max):** “Low-stock SKUs”, “Open transfers”, “Pending cycle counts”, “Inbound in <=3d”. Each chip drills into a saved view.
- **Primary workbench:** Data grid for the user’s role (e.g., *Low Stock* or *Open Orders*), **Saved Views** tabs, bulk actions, and an **inline filter rail**.
- **Right-side detail drawer:** Clicking a row opens a tabbed drawer (Locations, Movements, Replenishment, Notes) so users don’t lose context.

### Navigation & wayfinding
- **Saved views = first-class** (per user/team).  
- **Breadcrumbs** for deep screens: *Inventory › Low Stock › SKU 100234*.  
- **Command palette** for power users: “replenish 100234 @Dubai qty 40”.

### Tables (the workhorse)
- Virtualized rows, resizable/hidden columns, pin left/right, inline edit, multi-select, keyboard nav.  
- **Summary footer** (selected count, total qty), **export**, and **last updated** timestamp.

### Filters & state
- **Facet filters** (Location, Status, Supplier) with chips; one-click **Clear**.  
- **Optimistic updates** with **toast + Undo**; clear error banners with retry.  
- **Presence/locks** when someone is editing the same record.

### Mobile & scanning
- Responsive single column; bottom nav; barcode/camera flows for receive/transfer/count; offline queue for weak connectivity.

### Accessibility, i18n, theming
- WCAG AA contrast, full keyboard support, visible focus.  
- **RTL** (Arabic) and localized numbers/units/currency.  
- Theme tokens (light/dark); consistent iconography.

### Security & audit
- Field-level RBAC; sensitive fields masked; **History** tab in the drawer for changes and who made them.

#### Text shell sketch (main frame)
```text
┌──────────────────────────── Top Bar ────────────────────────────┐
│ LOGO | Inventory  ▸  Low Stock          [⌘K Search]  Env: PROD │
│       Breadcrumbs                              Alerts  Help  Ayub│
├── Left Nav ───────────────┬─────────────────────────────────────┤
│ ▸ Inventory               │  KPIs:  Low Stock(28)  Transfers(3) │
│ ▸ Replenishment           │  Inbound<=3d(12)  Cycle Counts(5)    │
│ ▸ Orders                  ├─────────────────────────────────────┤
│ ▸ Transfers               │  Saved Views: [Low Stock]* [By Loc] │
│ ▸ Catalog                 │  Filters: Location[DXB] Supplier[Nike]
│ ▸ Suppliers               │  ─────────────────────────────────── │
│ ▸ Reports                 │  ▢ SKU   Name          On-Hand  Min │
│ ▸ Admin                   │  ▢ 100234 AirMax 42    12       20  │
│ ★ Starred Views           │  ▢ 100236 AirMax 44     5       20  │
│                           │  [Bulk Actions ▾] [Replenish]        │
│                           ├───────────── Drawer (SKU 100234) ───│
│                           │ Locations | Movements | Replenish    │
│                           │ On-Hand by site, ETA, notes, history │
└───────────────────────────┴──────────────────────────────────────┘
```

---

## 2) Modern SaaS-ERP UI Trends (Inventory Context)

- **Role-based home:** task widgets (e.g., “20 SKUs below safety stock”, “3 pending cycle counts”), not a generic dashboard.  
- **Global search + command palette:** fuzzy SKU/UPC search, “/replenish 12345 @Dubai” style commands.  
- **Configurable data grids:** column pickers, saved views, pinned columns, density toggle, inline edit, bulk actions.  
- **Context drawers (not page jumps):** select a row → slide-in panel with details, stock by location, recent movements.  
- **Progressive disclosure:** simple first, advanced filters (facets: location, status, supplier) on demand.  
- **Keyboard-first:** shortcuts (Ctrl/⌘-K palette, E = edit, S = save, Shift+Click = multi-select).  
- **Realtime signals:** presence/locks, low-stock badges, inbound ETA chips; optimistic updates with **toast + Undo**.  
- **Mobile & scanning:** responsive layouts; camera/barcode scanning flows for receive/transfer/count.  
- **Assisted operations (AI):** reorder suggestions, anomaly flags, NL query → filter (“show Nike SKUs under min in Dubai”).  
- **Zero/empty states:** “No low-stock items” → CTA to adjust min/max or run forecast.  
- **Accessibility & i18n:** WCAG AA, large tap targets, RTL (Arabic), unit/number/date localization.  
- **Theming & performance:** light/dark, tokenized colors; **virtualized tables** for >10k rows.  
- **Notifications inbox:** actionable alerts with SLAs; per-user thresholds.  
- **Security & audit:** field-level RBAC, change history, export controls.

### Text UI wireframe — Inventory Overview
```text
┌──────────────────────────────────────────────────────────────────────────────┐
│  ▌▌  LOGO                Inventory | Dashboard | Orders | Replenishment      │
│  [Ctrl/⌘-K] Quick Search…           Alerts(3)     Help(?)     Ayub ▾         │
├──────────────────────────────────────────────────────────────────────────────┤
│ Filters: Location [Dubai ▾]  Status [All ▾]  Low Stock [<= Min ▾]  Supplier ▾│
│ [Save View ▾]  [Reset]  Chips:  • Low Stock  • Footwear   • ETA<3d            │
├──────────────────────────────────────────────────────────────────────────────┤
│ ▢  SKU        Name                     Loc   On-Hand  ATP  ReorderPt  Status │
│ ──────────────────────────────────────────────────────────────────────────── │
│ ▢  100234     AirMax 90 Black 42       DXB   12       3    20         LOW ! │
│ ▢  100235     AirMax 90 Black 43       DXB   28       10   20         OK     │
│ ▢  100236     AirMax 90 Black 44       DXB   5        0    20         LOW !  │
│ …                                                                              │
│ [Bulk Actions ▾]  [Replenish]  [Transfer]  [Print Labels]                      │
├───────────────────────────────┬───────────────────────────────────────────────┤
│ Row Details (slide-in)        │ Movements  |  Locations  |  Replenishment     │
│ ─────────────────────────────  │                                               │
│ SKU: 100234   Category: Shoes │ Locations:                                     │
│ Supplier: Nike                │  • Dubai Mall   On-Hand 12  Min 20  ETA 2d     │
│ Safety Stock: 20              │  • MOE          On-Hand  6  Min 15             │
│ Notes: Button order fix done. │ Movements (last 7d): In: 24  Out: 17           │
│ [Edit] [Adjust Min/Max]       │ [Create Replenishment] [Open IDD] [History]    │
└───────────────────────────────┴───────────────────────────────────────────────┘
Keyboard: Ctrl/⌘-K palette • ↑/↓ navigate • Enter details • E edit • Ctrl/⌘-S save
Toasts: “Reorder point updated — Undo”
```

### Command palette (quick actions)
```text
┌───────────────────────────────Ctrl/⌘-K────────────────────────────────────────┐
│ > replenish 100234 @Dubai qty 40                                            │
│   go to low-stock view     create transfer     open supplier "Nike"          │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Empty/zero state (Low-stock tab)
```text
Low-stock is clear ✅
No items under minimum today.
[Review min/max rules]  [Open forecast]  [Create scheduled report]
```

### Error/resilience example
```text
Update failed (network). Your change is saved offline.
[Retry]  [Discard]   — Last sync: 2m ago
```

### RTL preview (Arabic, sidebar flips right, text right-aligned)
```text
┌──────────────────────────────────────────────────────────────────────────────┐
│ Ayub ▾   مساعدة  تنبيهات(٣)     إدارة المخزون   [بحث سريع Ctrl/⌘-K]   الشعار│
├──────────────────────────────────────────────────────────────────────────────┤
│ المواقع [دبي ▾]  الحالة [الكل ▾]  الحد الأدنى ▾  المورّد ▾               │
│                                                                              │
│ الحالة  نقطة إعادة الطلب  ATP  المخزون  الموقع  الاسم           SKU        │
│ منخفض!  ٢٠                  ٣     ١٢      دبي    ٤٢ أسود AirMax 90  ١٠٠٢٣٤ │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 3) Implementation Tips
- Use a **grid layout** with a right-hand **detail drawer** to avoid full-page reloads.
- Treat the table as the “workbench”: **inline edits**, **bulk actions**, **saved views**.
- Ship a **shortcuts cheat** and a **command palette** early—power users will adopt them fast.
- Design **empty, loading (skeleton), and error** states deliberately.
- Plan **RTL** and **accessibility** up front (tab order, focus states, contrast).
