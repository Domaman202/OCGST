local io = require("io")
local inet = require("internet")
local math = require("math")
local bit32 = require("bit32")
local table = require("table")
local string = require("string")
local component = require("component")

local socket = inet.open("localhost", 25585)
local fss = {}

local function bti(bytes)
    return string.byte(string.sub(bytes, 1, 2))*256^3 + string.byte(string.sub(bytes, 2, 3))*256^2 + string.byte(string.sub(bytes, 3, 4))*256 + string.byte(string.sub(bytes, 4, 5))
end

local function itb(i)
    return string.char(bit32.band(bit32.rshift(i, 24), 0xFF), bit32.band(bit32.rshift(i, 16), 0xFF), bit32.band(bit32.rshift(i, 8), 0xFF), bit32.band(i, 0xFF))
end

local function readByte()
    return string.byte(socket:read(1))
end

local function readBytes()
    return socket:read(bti(socket:read(4)))
end

local function readPacket()
    return readByte(), readByte(), readBytes()
end

local function sendByte(byte)
    socket:write(string.char(bit32.band(byte, 0xFF)))
    socket:flush()
end

local function sendBytes(bytes)
    socket:write(itb(bytes:len()))
    socket:write(bytes)
    socket:flush()
end

for k, v in component.list("filesystem") do
    local fs = component.proxy(k)
    if fs.exists("ocgst") then
        table.insert(fss, fs)
    end
end

while true do
    local action, fs, path = readPacket()
    print(action, fs, path)
    fs = fss[fs]
    if action == 0 then
        print("[r] start")
        local handle, err = fs.open(path, "r")
        local size = fs.size(path)
        socket:write(itb(size))
        while size > 0 do
            local data = fs.read(handle, size)
            print(size, data:len())
            size = size - data:len()
            socket:write(data)
            socket:flush()
        end
        print("[r] complete")
        fs.close(handle)
    elseif action == 1 then
        print("[w] start")
        local handle, err = fs.open(path, "w")
        local size = bti(socket:read(4))
        while size > 0 do
            local data = socket:read(math.min(2048, size))
            print(size, data:len())
            size = size - data:len()
            fs.write(handle, data)
        end
        print("[w] complete")
        fs.close(handle)
    elseif action == 2 then
        local out = ""
        for k, v in ipairs(fss) do
            out = out..v.fsnode.name..";"
        end
        sendBytes(out)
    elseif action == 3 then
        sendByte(fs.isDirectory(path) and 1 or 0)
    elseif action == 4 then
        fs.makeDirectory(path)
    elseif action == 5 then
        fs.remove(path)
    end
    sendByte(0)
end