# Item Interactions

A Minecraft mod. Downloads can be found on [CurseForge](https://www.curseforge.com/members/fuzs_/projects)
and [Modrinth](https://modrinth.com/user/Fuzs).

![](https://raw.githubusercontent.com/Fuzss/modresources/main/pages/data/iteminteractions/banner.png)

## Outline

Item Interactions is a library mod that offers some great features to supercharge using items with an inventory
directly from your very own inventory without ever having to open the item's dedicated container. Those features are:

- Item tooltips with a rich preview of all inventory contents.
- Bundle-like behavior for inserting and extracting individual item stacks.
- Scroll through all items on the item's tooltip to choose which item to extract next.
- A tooltip next to the container item's tooltip for the item you have currently scrolled to in the container, great for
  telling apart tools with different enchantments!
- Drag above other items in your inventory while holding a container item to add all those items to its inventory, drag
  above empty slots to extract items from the container.

Support for individual items is fully data-driven via data packs using so-called item contents providers. This way items
from mods (mainly backpacks and larger
shulker boxes) can work, too, when corresponding providers are added.

While this library itself does not include any such providers adding new item capabilities,
the [Easy Shulker Boxes](https://github.com/Fuzss/easyshulkerboxes) mod can be used to do just that for a bunch of
vanilla items: all shulker boxes, all bundles, and the ender chest.

## Configuration (for Minecraft 1.21.1+)

All item contents providers are found at the following locations:
> `data/<namespace>/iteminteractions/item_container_providers/<path>.json` (for Minecraft 1.21.4+)

> `data/<namespace>/item_container_providers/<path>.json` (for Minecraft 1.21.1-1.21.3)

The placeholder `<namespace>:<path>` represents the arbitrary provider id.

A single provider can enable capabilities for one or many items, meaning there does not have to be one file per item,
nor do the file names necessarily have to correspond to the item names.

Depending on the kind of item that the provider is for, different types are available.

---

### Common fields

| Field             | Mandatory | Allowed Values                                                              | Explanation                                                                                       |
|-------------------|-----------|-----------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| `type`            | ✅         | `ResourceLocation`                                                          | The provide type id for this definition. Available provider types are listed and explained below. |
| `supported_items` | ✅         | `ResourceLocation` or `TagKey` or a list of `ResourceLocation` and `TagKey` | The container items this provider definition will apply to.                                       |

---

### Item contents fields (for Minecraft 1.21.8+)

| Field                    | Mandatory | Allowed Values                          | Explanation                                                                               |
|--------------------------|-----------|-----------------------------------------|-------------------------------------------------------------------------------------------|
| `disallow`               | ❌         | `true` or `false`                       | Turn the defined item list from including allowed items to excluding disallowed items.    |
| `filter_container_items` | ❌         | `true` or `false`                       | Prevent shulker boxes from being put into this item container.                            |
| `items`                  | ❌         | List of `ResourceLocation` and `TagKey` | A list of item ids or tag keys allowed or not allowed to be put into this container item. |

---

### Provider type: `iteminteractions:empty`

This type, when added to an item, does nothing and adds no new capabilities. It exists solely to allow data packs to
override existing providers to remove them again.

#### Available fields

There are no settings available for this type.

#### Example

> `data/minecraft/iteminteractions/item_contents_provider/empty.json`

```json
{
  "type": "iteminteractions:empty",
  "supported_items": "..."
}
```

---

### Provider type: `iteminteractions:container`

This type is used for most items that store an inventory. It can be used all the way from shulker boxes to even
backpacks from other mods.

#### Available fields (for Minecraft 1.21.8+)

| Field                     | Mandatory | Allowed Values                                                                                                                                                                                                                        | Explanation                                                                                                                                                                                          |
|---------------------------|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `background_color`        | ❌         | [ARGB](https://en.wikipedia.org/wiki/RGBA_color_model) as `Integer` or any of: `white`, `orange`, `magenta`, `light_blue`, `yellow`, `lime`, `pink`, `gray`, `light_gray`, `cyan`, `purple`, `blue`, `brown`, `green`, `red`, `black` | The background color used on the item tooltip, defaults to vanilla's gray container background color.                                                                                                |
| `equipment_slots`         | ❌         | Any of `any`, `mainhand`, `offhand`, `hand`, `feet`, `legs`, `chest`, `head`, `armor`, `body`, `saddle`                                                                                                                               | An inventory equipment slot the item needs to be placed in to allow for inventory interactions in survival mode. Useful for items that must be worn in some inventory slot to become usable.         |
| `interaction_permissions` | ❌         | `always`, `creative_only` or `never`                                                                                                                                                                                                  | Controls the game mode the player must be in for using any of the inventory interactions. Purely visual capabilities may still be usable. Also can prevent any interactions at all in any game mode. |
| `inventory_height`        | ✅         | `0 >`                                                                                                                                                                                                                                 | The number of vertical container slots; meaning the amount of rows in the item container screen.                                                                                                     |
| `inventory_width`         | ✅         | `0 >`                                                                                                                                                                                                                                 | The number of horizontal container slots; meaning the amount of columns in the item container screen.                                                                                                |
| `item_contents`           | ❌         | [Item Contents Definition](#item-contents-fields-for-minecraft-1218)                                                                                                                                                                  | An object for filtering items that can or cannot be put into this container item.                                                                                                                    |

#### Available fields (for Minecraft 1.21.1-1.21.7)

<details>

| Field                      | Mandatory | Allowed Values                                                                                                                                                                                                                        | Explanation                                                                                                                                                                                          |
|----------------------------|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `background_color`         | ❌         | [ARGB](https://en.wikipedia.org/wiki/RGBA_color_model) as `Integer` or any of: `white`, `orange`, `magenta`, `light_blue`, `yellow`, `lime`, `pink`, `gray`, `light_gray`, `cyan`, `purple`, `blue`, `brown`, `green`, `red`, `black` | The background color used on the item tooltip, defaults to vanilla's gray container background color.                                                                                                |
| `disallowed_item_contents` | ❌         | List of `ResourceLocation` and `TagKey`                                                                                                                                                                                               | A list of item ids or tag keys not allowed to be put into this container item.                                                                                                                       |
| `equipment_slots`          | ❌         | Any of `any`, `mainhand`, `offhand`, `hand`, `feet`, `legs`, `chest`, `head`, `armor`, `body`, `saddle`                                                                                                                               | An inventory equipment slot the item needs to be placed in to allow for inventory interactions in survival mode. Useful for items that must be worn in some inventory slot to become usable.         |
| `filter_container_items`   | ❌         | `true` or `false`                                                                                                                                                                                                                     | Prevent shulker boxes from being put into this item container.                                                                                                                                       |
| `interaction_permissions`  | ❌         | `always`, `creative_only` or `never`                                                                                                                                                                                                  | Controls the game mode the player must be in for using any of the inventory interactions. Purely visual capabilities may still be usable. Also can prevent any interactions at all in any game mode. |
| `inventory_height`         | ✅         | `0 >`                                                                                                                                                                                                                                 | The number of vertical container slots; meaning the amount of rows in the item container screen.                                                                                                     |
| `inventory_width`          | ✅         | `0 >`                                                                                                                                                                                                                                 | The number of horizontal container slots; meaning the amount of columns in the item container screen.                                                                                                |

</details>

#### Example

> `data/minecraft/iteminteractions/item_contents_provider/shulker_box.json`

```json
{
  "type": "iteminteractions:container",
  "equipment_slots": "any",
  "interaction_permissions": "always",
  "inventory_height": 3,
  "inventory_width": 9,
  "item_contents": {
    "disallow": true,
    "filter_container_items": true
  },
  "supported_items": "minecraft:shulker_box"
}
```

---

### Provider type: `iteminteractions:ender_chest`

This type is used for blocks and items providing access to the ender chest of a player.

#### Available fields

There are no settings available for this type.

#### Example

> `data/minecraft/iteminteractions/item_contents_provider/ender_chest.json`

```json
{
  "type": "iteminteractions:ender_chest",
  "supported_items": "minecraft:ender_chest"
}
```

---

### Provider type: `iteminteractions:bundle`

This type is used for bundle items. Instead of specifying inventory dimensions, the capacity multiplier must be set.

#### Available fields (for Minecraft 1.21.8+)

| Field                 | Mandatory | Allowed Values                                                                                                                                                                                                                        | Explanation                                                                                                                                |
|-----------------------|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `background_color`    | ❌         | [ARGB](https://en.wikipedia.org/wiki/RGBA_color_model) as `Integer` or any of: `white`, `orange`, `magenta`, `light_blue`, `yellow`, `lime`, `pink`, `gray`, `light_gray`, `cyan`, `purple`, `blue`, `brown`, `green`, `red`, `black` | The background color used on the item tooltip, defaults to vanilla's gray container background color.                                      |
| `capacity_multiplier` | ✅         | `0 >`                                                                                                                                                                                                                                 | Scale for [capacity](https://minecraft.wiki/w/Bundle#Usage) of the bundle (the available weight); multiplied by base capacity which is 64. |
| `item_contents`       | ❌         | [Item Contents Definition](#item-contents-fields-for-minecraft-1218)                                                                                                                                                                  | An object for filtering items that can or cannot be put into this container item.                                                          |

#### Available fields (for Minecraft 1.21.1-1.21.7)

<details>

| Field                      | Mandatory | Allowed Values                                                                                                                                                                                                                        | Explanation                                                                                                                                |
|----------------------------|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `background_color`         | ❌         | [ARGB](https://en.wikipedia.org/wiki/RGBA_color_model) as `Integer` or any of: `white`, `orange`, `magenta`, `light_blue`, `yellow`, `lime`, `pink`, `gray`, `light_gray`, `cyan`, `purple`, `blue`, `brown`, `green`, `red`, `black` | The background color used on the item tooltip, defaults to vanilla's gray container background color.                                      |
| `capacity_multiplier`      | ✅         | `0 >`                                                                                                                                                                                                                                 | Scale for [capacity](https://minecraft.wiki/w/Bundle#Usage) of the bundle (the available weight); multiplied by base capacity which is 64. |
| `disallowed_item_contents` | ❌         | List of `ResourceLocation` and `TagKey`                                                                                                                                                                                               | A list of item ids or tag keys not allowed to be put into this container item. Shulker boxes are always prevented from being stored.       |

</details>

#### Example

> `data/minecraft/iteminteractions/item_contents_provider/bundle.json`

```json
{
  "type": "iteminteractions:bundle",
  "background_color": "brown",
  "capacity_multiplier": 1,
  "item_contents": {
    "disallow": true,
    "filter_container_items": true
  },
  "supported_items": "minecraft:bundle"
}
```
