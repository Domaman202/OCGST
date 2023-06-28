local inet = require("internet")
local json = (loadfile "json.lua")()
local component = require("component")

local socket = inet.socket("localhost", 25585)
local fss = {}

for addr in component.list("filesystem") do
    local fs = component.proxy(addr)
    if (fs.exists("ocgst")) then
        fss[addr:sub(1, 3)] = fs
    end
end

while true do
    ::continue::
    local input = socket:read(2147483647)
    if input == nil then
        print("Connection error!")
        break
    elseif input == "" then
        goto continue
    end

    print("[Input]"..input)
    local packet = json.decode(input)

    local function sendpacket(act, data)
        socket:write(json.encode({ id = packet.id, action = act, data = data }))
    end

    if packet.action == "hello" then
        sendpacket("R", component.computer.address:sub(1,3))
    elseif packet.action == "drives" then
        local addrs = {}
        for addr, _ in pairs(fss) do
            table.insert(addrs, addr)
        end
        sendpacket("R", addrs)
    elseif packet.action == "mkdir" then
        sendpacket("R", fss[packet.data.fs].makeDirectory(packet.data.path))
    elseif packet.action == "exists" then
        sendpacket("R", fss[packet.data.fs].exists(packet.data.path))
    elseif packet.action == "write" then
        local fs = fss[packet.data.fs]
        local handle = fs.open(packet.data.path, "w")
        sendpacket("R", fs.write(handle, packet.data.data))
        fs.close(handle)
    elseif packet.action == "space" then
        sendpacket("R", fss[packet.data.fs].spaceTotal())
    elseif packet.action == "isdir" then
        sendpacket("R", fss[packet.data.fs].isDirectory(packet.data.path))
    elseif packet.action == "rename" then
        sendpacket("R", fss[packet.data.fs].rename(packet.data.path, packet.data.to))
    elseif packet.action == "list" then
        sendpacket("R", fss[packet.data.fs].list(packet.data.path))
    elseif packet.action == "rm" then
        sendpacket("R", fss[packet.data.fs].remove(packet.data.path))
    elseif packet.action == "size" then
        sendpacket("R", fss[packet.data.fs].size(packet.data.path))
    elseif packet.action == "read" then
        local fs = fss[packet.data.fs]
        local handle = fs.open(packet.data.path, "r")
        sendpacket("R", fs.read(handle, 2147483647))
        fs.close(handle)
    end
end
