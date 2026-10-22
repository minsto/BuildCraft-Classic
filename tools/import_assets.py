import json
import os
import shutil

JAR = r"C:\Users\chopi\Downloads\buildcraft-all-8.0.0.jar_Decompiler.com"
ROOT = r"C:\Mes mods crée\mm\buildcraft-classic-template-1.21.1"
ASSETS_OUT = os.path.join(ROOT, "common", "src", "main", "resources", "assets")
DATA_OUT = os.path.join(ROOT, "common", "src", "main", "resources", "data")
REFERENCE = os.path.join(ROOT, "reference")
SOURCE_REPO = r"C:\Users\chopi\OneDrive\Documents\GitHub\BuildCraft"

VANILLA_PARENTS = {
    "block/block": "minecraft:block/block",
    "block/cube": "minecraft:block/cube",
    "block/cube_all": "minecraft:block/cube_all",
    "block/orientable": "minecraft:block/orientable",
    "block/torch_wall": "minecraft:block/torch_wall",
    "item/generated": "minecraft:item/generated",
    "item/handheld": "minecraft:item/handheld",
    "builtin/generated": "minecraft:item/generated",
    "builtin/entity": "minecraft:builtin/entity",
}


def rewrite_json(text):
    text = text.replace("minecraft:blocks/", "minecraft:block/")
    text = text.replace("minecraft:items/", "minecraft:item/")
    try:
        data = json.loads(text)
    except Exception:
        return text
    if isinstance(data, dict) and isinstance(data.get("parent"), str):
        parent = data["parent"]
        if parent in VANILLA_PARENTS:
            data["parent"] = VANILLA_PARENTS[parent]
            return json.dumps(data, indent=2) + "\n"
    return text


def copy_assets():
    src = os.path.join(JAR, "assets")
    if os.path.isdir(ASSETS_OUT):
        shutil.rmtree(ASSETS_OUT)
    os.makedirs(ASSETS_OUT)
    for dirpath, _, filenames in os.walk(src):
        for name in filenames:
            if name.endswith(".lang"):
                continue
            source = os.path.join(dirpath, name)
            relative = os.path.relpath(source, src)
            target = os.path.join(ASSETS_OUT, relative)
            os.makedirs(os.path.dirname(target), exist_ok=True)
            if name.endswith(".json"):
                text = open(source, encoding="utf-8").read()
                open(target, "w", encoding="utf-8", newline="\n").write(rewrite_json(text))
            else:
                shutil.copy2(source, target)


def convert_langs():
    src = os.path.join(JAR, "assets", "buildcraft", "lang")
    out = os.path.join(ASSETS_OUT, "buildcraft", "lang")
    os.makedirs(out, exist_ok=True)
    for name in os.listdir(src):
        if not name.endswith(".lang"):
            continue
        locale = name[:-5].lower()
        entries = {}
        for line in open(os.path.join(src, name), encoding="utf-8", errors="replace"):
            stripped = line.strip()
            if not stripped or stripped.startswith("#") or "=" not in stripped:
                continue
            key, value = stripped.split("=", 1)
            entries[key] = value
        open(os.path.join(out, locale + ".json"), "w", encoding="utf-8", newline="\n").write(
            json.dumps(entries, ensure_ascii=False, indent=2) + "\n"
        )


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, "w", encoding="utf-8", newline="\n").write(json.dumps(data, indent=2) + "\n")


def faces(texture):
    return {side: {"texture": texture} for side in ("down", "up", "north", "south", "west", "east")}


def pipe_models():
    kinds = {
        "pipe_wood_item": "wood_item_clear",
        "pipe_cobble_item": "cobblestone_item",
        "pipe_stone_item": "stone_item",
        "pipe_iron_item": "iron_item_clear",
        "pipe_gold_item": "gold_item",
        "pipe_diamond_item": "diamond_item",
        "pipe_wood_fluid": "wood_fluid_clear",
        "pipe_cobble_fluid": "cobblestone_fluid",
        "pipe_wood_power": "wood_power_clear",
        "pipe_cobble_power": "cobblestone_power",
    }
    base = os.path.join(ASSETS_OUT, "buildcrafttransport")
    for path, texture in kinds.items():
        tex = "buildcrafttransport:pipes/" + texture
        core = {
            "parent": "minecraft:block/block",
            "textures": {"particle": tex, "pipe": tex},
            "elements": [{"from": [4, 4, 4], "to": [12, 12, 12], "faces": faces("#pipe")}],
        }
        arm = {
            "parent": "minecraft:block/block",
            "textures": {"particle": tex, "pipe": tex},
            "elements": [{"from": [4, 4, 0], "to": [12, 12, 4], "faces": faces("#pipe")}],
        }
        write_json(os.path.join(base, "models", "block", "pipe", path + "_core.json"), core)
        write_json(os.path.join(base, "models", "block", "pipe", path + "_arm.json"), arm)
        core_model = "buildcrafttransport:block/pipe/" + path + "_core"
        arm_model = "buildcrafttransport:block/pipe/" + path + "_arm"
        state = {
            "multipart": [
                {"apply": {"model": core_model}},
                {"when": {"north": "true"}, "apply": {"model": arm_model}},
                {"when": {"south": "true"}, "apply": {"model": arm_model, "y": 180}},
                {"when": {"west": "true"}, "apply": {"model": arm_model, "y": 270}},
                {"when": {"east": "true"}, "apply": {"model": arm_model, "y": 90}},
                {"when": {"up": "true"}, "apply": {"model": arm_model, "x": 270}},
                {"when": {"down": "true"}, "apply": {"model": arm_model, "x": 90}},
            ]
        }
        write_json(os.path.join(base, "blockstates", path + ".json"), state)
        write_json(os.path.join(base, "models", "item", path + ".json"), {"parent": core_model})


def cube(texture_map):
    return {"parent": "minecraft:block/cube", "textures": texture_map}


def simple_state(model):
    return {"variants": {"": {"model": model}}}


def facing_state(model):
    return {"variants": {f"facing={direction}": {"model": model} for direction in ("north", "south", "west", "east", "up", "down")}}


def block_item(namespace, path, parent):
    write_json(os.path.join(ASSETS_OUT, namespace, "models", "item", path + ".json"), {"parent": parent})


def our_models():
    engines = {
        "engine_stirling": "buildcraftenergy:blocks/engine/stone/side",
        "engine_combustion": "buildcraftenergy:blocks/engine/iron/side",
    }
    for path, texture in engines.items():
        model = cube({
            "particle": texture,
            "down": texture.replace("/side", "/back"),
            "up": texture,
            "north": texture,
            "south": texture,
            "west": texture,
            "east": texture,
        })
        write_json(os.path.join(ASSETS_OUT, "buildcraftenergy", "models", "block", path + ".json"), model)
        write_json(os.path.join(ASSETS_OUT, "buildcraftenergy", "blockstates", path + ".json"), facing_state("buildcraftenergy:block/" + path))
        block_item("buildcraftenergy", path, "buildcraftenergy:block/" + path)

    marker = {"parent": "minecraft:block/cube_all", "textures": {"all": "buildcraftcore:blocks/marker_volume"}}
    write_json(os.path.join(ASSETS_OUT, "buildcraftcore", "models", "block", "marker_volume.json"), marker)
    write_json(os.path.join(ASSETS_OUT, "buildcraftcore", "blockstates", "marker_volume.json"), simple_state("buildcraftcore:block/marker_volume"))
    block_item("buildcraftcore", "marker_volume", "buildcraftcore:block/marker_volume")

    pointed = {
        ("buildcraftfactory", "tank"): "buildcraftfactory:block/tank",
        ("buildcraftfactory", "pump"): "buildcraftfactory:block/pump",
        ("buildcraftfactory", "mining_well"): "buildcraftfactory:block/mining_well",
        ("buildcraftbuilders", "quarry"): "buildcraftbuilders:block/quarry",
        ("buildcraftbuilders", "filler"): "buildcraftbuilders:block/filler/main",
        ("buildcraftbuilders", "builder"): "buildcraftbuilders:block/builder/main",
        ("buildcraftsilicon", "laser"): "buildcraftsilicon:block/laser",
        ("buildcraftsilicon", "assembly_table"): "buildcraftsilicon:block/table/assembly",
        ("buildcraftrobotics", "zone_planner"): "buildcraftrobotics:block/zone_planner",
    }
    for (namespace, path), model in pointed.items():
        write_json(os.path.join(ASSETS_OUT, namespace, "blockstates", path + ".json"), simple_state(model))
        block_item(namespace, path, model)

    generated = {
        ("buildcraftcore", "gear_wood"): "buildcraftcore:items/gear_wood",
        ("buildcraftcore", "gear_stone"): "buildcraftcore:items/gear_stone",
        ("buildcraftcore", "gear_iron"): "buildcraftcore:items/gear_iron",
        ("buildcraftcore", "gear_gold"): "buildcraftcore:items/gear_gold",
        ("buildcraftcore", "gear_diamond"): "buildcraftcore:items/gear_diamond",
        ("buildcraftsilicon", "chipset_redstone"): "buildcraftsilicon:items/redstone_chipset/red",
        ("buildcraftsilicon", "chipset_iron"): "buildcraftsilicon:items/redstone_chipset/iron",
        ("buildcraftsilicon", "chipset_gold"): "buildcraftsilicon:items/redstone_chipset/gold",
        ("buildcraftsilicon", "chipset_diamond"): "buildcraftsilicon:items/redstone_chipset/diamond",
        ("buildcraftsilicon", "gate"): "buildcraftsilicon:items/gates/gate_logic_and",
        ("buildcraftrobotics", "robot"): "buildcraftbuilders:robot",
    }
    for (namespace, path), texture in generated.items():
        write_json(
            os.path.join(ASSETS_OUT, namespace, "models", "item", path + ".json"),
            {"parent": "minecraft:item/generated", "textures": {"layer0": texture}},
        )


def shaped(pattern, key, result, count=1):
    return {
        "type": "minecraft:crafting_shaped",
        "pattern": pattern,
        "key": {symbol: {"item": item} for symbol, item in key.items()},
        "result": {"id": result, "count": count},
    }


def shapeless(ingredients, result, count=1):
    return {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [{"item": item} for item in ingredients],
        "result": {"id": result, "count": count},
    }


def recipe(namespace, name, data):
    write_json(os.path.join(DATA_OUT, namespace, "recipe", name + ".json"), data)


def loot(namespace, name):
    write_json(os.path.join(DATA_OUT, namespace, "loot_table", "blocks", name + ".json"), {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:item", "name": namespace + ":" + name}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
    })


def our_data():
    if os.path.isdir(DATA_OUT):
        shutil.rmtree(DATA_OUT)
    recipe("buildcraftcore", "gear_wood", shaped([" S ", "S S", " S "], {"S": "minecraft:stick"}, "buildcraftcore:gear_wood"))
    recipe("buildcraftcore", "gear_stone", shaped(["CCC", "CGC", "CCC"], {"C": "minecraft:cobblestone", "G": "buildcraftcore:gear_wood"}, "buildcraftcore:gear_stone"))
    recipe("buildcraftcore", "gear_iron", shaped(["III", "IGI", "III"], {"I": "minecraft:iron_ingot", "G": "buildcraftcore:gear_stone"}, "buildcraftcore:gear_iron"))
    recipe("buildcraftcore", "gear_gold", shaped(["III", "IGI", "III"], {"I": "minecraft:gold_ingot", "G": "buildcraftcore:gear_iron"}, "buildcraftcore:gear_gold"))
    recipe("buildcraftcore", "gear_diamond", shaped(["III", "IGI", "III"], {"I": "minecraft:diamond", "G": "buildcraftcore:gear_gold"}, "buildcraftcore:gear_diamond"))
    recipe("buildcraftcore", "wrench", shaped(["I I", " G ", " I "], {"I": "minecraft:iron_ingot", "G": "buildcraftcore:gear_stone"}, "buildcraftcore:wrench"))
    recipe("buildcraftcore", "marker_volume", shaped([" G ", "RLR", " G "], {"G": "minecraft:glass", "R": "minecraft:redstone_torch", "L": "minecraft:lapis_lazuli"}, "buildcraftcore:marker_volume"))
    recipe("buildcraftenergy", "engine_stirling", shaped(["CCC", "CGC", " P "], {"C": "minecraft:cobblestone", "G": "buildcraftcore:gear_stone", "P": "minecraft:piston"}, "buildcraftenergy:engine_stirling"))
    recipe("buildcraftenergy", "engine_combustion", shaped(["III", "IGI", " P "], {"I": "minecraft:iron_ingot", "G": "buildcraftcore:gear_iron", "P": "minecraft:piston"}, "buildcraftenergy:engine_combustion"))
    pipes = [
        ("pipe_wood_item", "minecraft:oak_planks"),
        ("pipe_cobble_item", "minecraft:cobblestone"),
        ("pipe_stone_item", "minecraft:stone"),
        ("pipe_iron_item", "minecraft:iron_ingot"),
        ("pipe_gold_item", "minecraft:gold_ingot"),
        ("pipe_diamond_item", "minecraft:diamond"),
    ]
    for name, material in pipes:
        recipe("buildcrafttransport", name, shaped(["G", "M", "G"], {"G": "minecraft:glass", "M": material}, "buildcrafttransport:" + name, 8))
    recipe("buildcrafttransport", "pipe_wood_fluid", shapeless(["buildcrafttransport:pipe_wood_item", "minecraft:green_dye"], "buildcrafttransport:pipe_wood_fluid"))
    recipe("buildcrafttransport", "pipe_cobble_fluid", shapeless(["buildcrafttransport:pipe_cobble_item", "minecraft:green_dye"], "buildcrafttransport:pipe_cobble_fluid"))
    recipe("buildcrafttransport", "pipe_wood_power", shapeless(["buildcrafttransport:pipe_wood_item", "minecraft:redstone"], "buildcrafttransport:pipe_wood_power"))
    recipe("buildcrafttransport", "pipe_cobble_power", shapeless(["buildcrafttransport:pipe_cobble_item", "minecraft:redstone"], "buildcrafttransport:pipe_cobble_power"))
    recipe("buildcraftfactory", "tank", shaped(["G G", "G G", "GGG"], {"G": "minecraft:glass"}, "buildcraftfactory:tank"))
    recipe("buildcraftfactory", "mining_well", shaped(["IRI", "IGI", " P "], {"I": "minecraft:iron_ingot", "R": "minecraft:redstone", "G": "buildcraftcore:gear_iron", "P": "minecraft:piston"}, "buildcraftfactory:mining_well"))
    recipe("buildcraftfactory", "pump", shaped(["ITI", "IMI", "I I"], {"I": "minecraft:iron_ingot", "T": "buildcraftfactory:tank", "M": "buildcraftfactory:mining_well"}, "buildcraftfactory:pump"))
    recipe("buildcraftbuilders", "filler", shaped([" M ", "MCM", " M "], {"M": "buildcraftcore:marker_volume", "C": "minecraft:chest"}, "buildcraftbuilders:filler"))
    recipe("buildcraftbuilders", "builder", shaped(["D", "F"], {"D": "buildcraftcore:gear_diamond", "F": "buildcraftbuilders:filler"}, "buildcraftbuilders:builder"))
    recipe("buildcraftbuilders", "quarry", shaped(["GDG", "IPI", "GDG"], {"G": "buildcraftcore:gear_gold", "D": "minecraft:diamond_pickaxe", "I": "buildcraftcore:gear_iron", "P": "minecraft:piston"}, "buildcraftbuilders:quarry"))
    recipe("buildcraftsilicon", "laser", shaped(["OR", "D ", "OR"], {"O": "minecraft:obsidian", "R": "minecraft:redstone", "D": "minecraft:diamond"}, "buildcraftsilicon:laser"))
    recipe("buildcraftsilicon", "assembly_table", shaped(["ORO", "RDR", "ORO"], {"O": "minecraft:obsidian", "R": "minecraft:redstone", "D": "minecraft:diamond"}, "buildcraftsilicon:assembly_table"))
    recipe("buildcraftsilicon", "gate", shaped(["R", "C"], {"R": "minecraft:redstone", "C": "buildcraftsilicon:chipset_redstone"}, "buildcraftsilicon:gate"))
    recipe("buildcraftrobotics", "zone_planner", shaped(["RMR", " C ", "RMR"], {"R": "minecraft:redstone", "M": "buildcraftcore:marker_volume", "C": "minecraft:chest"}, "buildcraftrobotics:zone_planner"))
    recipe("buildcraftrobotics", "robot", shaped(["GIG", "IRI", "GIG"], {"G": "minecraft:gold_ingot", "I": "minecraft:iron_ingot", "R": "minecraft:redstone"}, "buildcraftrobotics:robot"))
    blocks = [
        ("buildcraftenergy", "engine_stirling"),
        ("buildcraftenergy", "engine_combustion"),
        ("buildcraftcore", "marker_volume"),
        ("buildcrafttransport", "pipe_wood_item"),
        ("buildcrafttransport", "pipe_cobble_item"),
        ("buildcrafttransport", "pipe_stone_item"),
        ("buildcrafttransport", "pipe_iron_item"),
        ("buildcrafttransport", "pipe_gold_item"),
        ("buildcrafttransport", "pipe_diamond_item"),
        ("buildcrafttransport", "pipe_wood_fluid"),
        ("buildcrafttransport", "pipe_cobble_fluid"),
        ("buildcrafttransport", "pipe_wood_power"),
        ("buildcrafttransport", "pipe_cobble_power"),
        ("buildcraftfactory", "tank"),
        ("buildcraftfactory", "pump"),
        ("buildcraftfactory", "mining_well"),
        ("buildcraftbuilders", "quarry"),
        ("buildcraftbuilders", "filler"),
        ("buildcraftbuilders", "builder"),
        ("buildcraftsilicon", "laser"),
        ("buildcraftsilicon", "assembly_table"),
        ("buildcraftrobotics", "zone_planner"),
    ]
    for namespace, name in blocks:
        loot(namespace, name)


def our_lang():
    en = {
        "itemGroup.buildcraftclassic": "BuildCraft Classic",
        "item.buildcraftcore.wrench": "Wrench",
        "item.buildcraftcore.gear_wood": "Wooden Gear",
        "item.buildcraftcore.gear_stone": "Stone Gear",
        "item.buildcraftcore.gear_iron": "Iron Gear",
        "item.buildcraftcore.gear_gold": "Gold Gear",
        "item.buildcraftcore.gear_diamond": "Diamond Gear",
        "block.buildcraftcore.marker_volume": "Volume Marker",
        "block.buildcraftenergy.engine_stirling": "Stirling Engine",
        "block.buildcraftenergy.engine_combustion": "Combustion Engine",
        "block.buildcrafttransport.pipe_wood_item": "Wooden Item Pipe",
        "block.buildcrafttransport.pipe_cobble_item": "Cobblestone Item Pipe",
        "block.buildcrafttransport.pipe_stone_item": "Stone Item Pipe",
        "block.buildcrafttransport.pipe_iron_item": "Iron Item Pipe",
        "block.buildcrafttransport.pipe_gold_item": "Golden Item Pipe",
        "block.buildcrafttransport.pipe_diamond_item": "Diamond Item Pipe",
        "block.buildcrafttransport.pipe_wood_fluid": "Wooden Fluid Pipe",
        "block.buildcrafttransport.pipe_cobble_fluid": "Cobblestone Fluid Pipe",
        "block.buildcrafttransport.pipe_wood_power": "Wooden Power Pipe",
        "block.buildcrafttransport.pipe_cobble_power": "Cobblestone Power Pipe",
        "item.buildcraftsilicon.gate": "Gate",
        "item.buildcraftsilicon.chipset_redstone": "Redstone Chipset",
        "item.buildcraftsilicon.chipset_iron": "Iron Chipset",
        "item.buildcraftsilicon.chipset_gold": "Gold Chipset",
        "item.buildcraftsilicon.chipset_diamond": "Diamond Chipset",
        "block.buildcraftfactory.tank": "Tank",
        "block.buildcraftfactory.pump": "Pump",
        "block.buildcraftfactory.mining_well": "Mining Well",
        "block.buildcraftbuilders.quarry": "Quarry",
        "block.buildcraftbuilders.filler": "Filler",
        "block.buildcraftbuilders.builder": "Builder",
        "block.buildcraftsilicon.laser": "Laser",
        "block.buildcraftsilicon.assembly_table": "Assembly Table",
        "block.buildcraftrobotics.zone_planner": "Zone Planner",
        "item.buildcraftrobotics.robot": "Robot",
    }
    fr = {
        "itemGroup.buildcraftclassic": "BuildCraft Classic",
        "item.buildcraftcore.wrench": "Clé à molette",
        "item.buildcraftcore.gear_wood": "Engrenage en bois",
        "item.buildcraftcore.gear_stone": "Engrenage en pierre",
        "item.buildcraftcore.gear_iron": "Engrenage en fer",
        "item.buildcraftcore.gear_gold": "Engrenage en or",
        "item.buildcraftcore.gear_diamond": "Engrenage en diamant",
        "block.buildcraftcore.marker_volume": "Marqueur de volume",
        "block.buildcraftenergy.engine_stirling": "Moteur Stirling",
        "block.buildcraftenergy.engine_combustion": "Moteur à combustion",
        "block.buildcrafttransport.pipe_wood_item": "Tuyau d'objets en bois",
        "block.buildcrafttransport.pipe_cobble_item": "Tuyau d'objets en cobblestone",
        "block.buildcrafttransport.pipe_stone_item": "Tuyau d'objets en pierre",
        "block.buildcrafttransport.pipe_iron_item": "Tuyau d'objets en fer",
        "block.buildcrafttransport.pipe_gold_item": "Tuyau d'objets en or",
        "block.buildcrafttransport.pipe_diamond_item": "Tuyau d'objets en diamant",
        "block.buildcrafttransport.pipe_wood_fluid": "Tuyau de fluides en bois",
        "block.buildcrafttransport.pipe_cobble_fluid": "Tuyau de fluides en cobblestone",
        "block.buildcrafttransport.pipe_wood_power": "Tuyau d'énergie en bois",
        "block.buildcrafttransport.pipe_cobble_power": "Tuyau d'énergie en cobblestone",
        "item.buildcraftsilicon.gate": "Porte logique",
        "item.buildcraftsilicon.chipset_redstone": "Puce de redstone",
        "item.buildcraftsilicon.chipset_iron": "Puce en fer",
        "item.buildcraftsilicon.chipset_gold": "Puce en or",
        "item.buildcraftsilicon.chipset_diamond": "Puce en diamant",
        "block.buildcraftfactory.tank": "Réservoir",
        "block.buildcraftfactory.pump": "Pompe",
        "block.buildcraftfactory.mining_well": "Puits de mine",
        "block.buildcraftbuilders.quarry": "Carrière",
        "block.buildcraftbuilders.filler": "Remplisseur",
        "block.buildcraftbuilders.builder": "Constructeur",
        "block.buildcraftsilicon.laser": "Laser",
        "block.buildcraftsilicon.assembly_table": "Table d'assemblage",
        "block.buildcraftrobotics.zone_planner": "Planificateur de zone",
        "item.buildcraftrobotics.robot": "Robot",
    }
    write_json(os.path.join(ASSETS_OUT, "buildcraftclassic", "lang", "en_us.json"), en)
    write_json(os.path.join(ASSETS_OUT, "buildcraftclassic", "lang", "fr_fr.json"), fr)


def reference_and_licenses():
    os.makedirs(REFERENCE, exist_ok=True)
    for name in ("changelog", "changelog_compat", "data", "README.md", "mcmod.info", "versions.txt", "META-INF"):
        source = os.path.join(JAR, name)
        target = os.path.join(REFERENCE, name)
        if os.path.isdir(source):
            if os.path.isdir(target):
                shutil.rmtree(target)
            shutil.copytree(source, target)
        elif os.path.isfile(source):
            shutil.copy2(source, target)
    os.makedirs(os.path.join(ROOT, "buildcraft_resources"), exist_ok=True)
    shutil.copy2(os.path.join(JAR, "LICENSE.BUILDCRAFT"), os.path.join(ROOT, "buildcraft_resources", "LICENSE.BUILDCRAFT"))
    license_new = os.path.join(SOURCE_REPO, "LICENSE-NEW")
    shutil.copy2(license_new, os.path.join(ROOT, "LICENSE"))
    old_src = os.path.join(ROOT, "src")
    if os.path.isdir(old_src):
        shutil.rmtree(old_src)


def verify():
    missing = []
    for dirpath, _, filenames in os.walk(os.path.join(ASSETS_OUT)):
        for name in filenames:
            if not name.endswith(".json"):
                continue
            path = os.path.join(dirpath, name)
            try:
                data = json.loads(open(path, encoding="utf-8").read())
            except Exception:
                continue
            textures = []
            if isinstance(data, dict):
                collect_textures(data.get("textures"), textures)
            for texture in textures:
                if texture.startswith("#") or ":" not in texture:
                    continue
                namespace, tex = texture.split(":", 1)
                png = os.path.join(ASSETS_OUT, namespace, "textures", tex + ".png")
                if not os.path.isfile(png):
                    missing.append(texture + " from " + os.path.relpath(path, ASSETS_OUT))
    print("missing textures", len(missing))
    for item in missing[:40]:
        print(" MISSING", item)


def collect_textures(value, out):
    if isinstance(value, str):
        out.append(value)
    elif isinstance(value, dict):
        for child in value.values():
            collect_textures(child, out)


def main():
    copy_assets()
    convert_langs()
    pipe_models()
    our_models()
    our_data()
    our_lang()
    reference_and_licenses()
    verify()
    print("assets imported")


if __name__ == "__main__":
    main()
