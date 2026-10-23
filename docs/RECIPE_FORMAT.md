# Recipe files

Recipes decide **whether the grid matches**. Outputs (see `COMPOSITE_FORMAT.md`) decide **what
comes out**. Recipe files live in `plugins/SkysongCrafting/recipes/<shaped|shapeless|smelting>/`
with a `.yml` or `.yaml` extension; the folder is only for organisation, the `recipe_type` key
decides how the file is read.

## Common keys

| Key | Meaning |
|---|---|
| `recipe_type` | `SHAPED`, `SHAPELESS`, `SHAPED_MARKING`, `SHAPELESS_MARKING` |
| `icon` | Material shown on the craft button |
| `name` | Recipe name on the craft button (MiniMessage) |
| `table_type` | Key of the workstation (from `workstations.yml`) this recipe can be crafted on, or `ANY` |
| `sound` | Sound key played on craft, e.g. `entity.villager.work_toolsmith` |
| `output` | `type:file`, e.g. `basic:iron_ingot` or `composite:add_layer` |
| `skill_requirements` | List of `skill:level` from SkysongStats, e.g. `["metalworking:16"]`; may be empty |

## Ingredients

Ingredients are written as the **crafting id** of the item (its SkysongItems `material_id` tag).

- `SHAPED`: `pattern` is a 5×5 grid of ids, `""` for an empty slot.
- `SHAPELESS`: `inputs` is a list of ids in any order; `dye*2` means two slots holding dye.
- `#tag_id` in either accepts any item listed in `tags/<file>.yaml`.

### Consumption suffixes

By default one item is taken from every non-empty slot when the recipe is crafted. A suffix on
the ingredient changes that:

| Written as | Effect |
|---|---|
| `wand^2` | reduce the item's `charges` tag by 2 instead of consuming it (an item without charges is consumed normally, with a console warning) |
| `anvil!keep` or `anvil^0` | leave the item untouched |
| `dye*2^1` / `dye^1*2` | shapeless: both dye slots lose one charge |

In a shapeless recipe the rule belongs to the ingredient id, so **every** slot holding that id is
consumed the same way. Listing the same id twice with different suffixes refuses the recipe.

## Marking (for composite outputs)

`SHAPED_MARKING` and `SHAPELESS_MARKING` additionally say which ingredients the output may read:

```yaml
marking_pattern:            # shaped: same 5x5 layout as pattern (a flat list of 25 also works)
  - [ false,false,false,false,false ]
  - [ false,false,true, false,false ]
  ...
marking_pattern: [ true, false ]   # shapeless: parallel to inputs
```

Marked items are numbered `0, 1, 2…` in grid order, counting only marked slots. A composite
template refers to them as `marked:0`, `marked:1`, …

## Conditions

Any recipe may add requirements on its marked ingredients, checked when the craft button is drawn
and again when it is clicked:

```yaml
conditions:
  - item: "marked:0"          # marked:N
    check: "tag:layer_count"  # tag:TAG_ID | material | amount | cmd:float:I | cmd:colors | ...
    operator: "<"             # == != < > <= >=
    value: 8
```

`check` accepts anything a composite placeholder accepts after the `;`. When a condition fails the
craft button turns yellow and its lore shows the condition and the actual value, e.g.
`marked:0 tag:layer_count < 8 (is 8)`. A malformed condition refuses the recipe at load with a
warning naming the file and the entry number.
