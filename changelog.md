TODO: 

1) Detect blocks with light emission between 1 and 15
2) Create a config to list all of the detected blocks with default value
3)
Generate JSON 
- emitters.json - CONTENT below:
{
"minecraft:torch": "#00FF00", // color in hex
"minecraft:red_candle": "red", // dye name
"minecraft:redstone_lamp": [ 0, 255, 255 ],
"minecraft:soul_torch": "purple;5", // override light level emission
"minecraft:oak_leaves": "light_blue;F" // value after ';' is a hex number from 0 to F
}

Location: assets\glowworks\light\emitters.json

