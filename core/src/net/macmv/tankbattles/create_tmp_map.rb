
# ************************
# Run from this directory!
# ************************

# mkdir and build proto for ruby, such that I can easily make a script to generate a map
# this needs ./gradelw installDist to be called first

`mkdir -p ../../../../build/generated/source/proto/ruby/`
`protoc ./lib/proto/mainProto.proto --ruby_out ../../../../build/generated/source/proto/ruby/`

require_relative "../../../../build/generated/source/proto/ruby/lib/proto/mainProto_pb.rb"

width = 10
height = 1
length = 20

map = TerrainMap.new
map.width = width
map.height = height
map.length = length

height.times do |y|
  plane = TerrainMap::Plane.new
  width.times do |x|
    row = TerrainMap::Plane::Row.new
    length.times do |z|
      tile = TerrainMap::Tile.new
      pos = Point3.new(x: x, y: y, z: z)
      tile.type = TerrainMap::Tile::Type::ROCK
      tile.pos = pos
      row.tiles[z] = tile
    end
    plane.rows[x] = row
  end
  map.planes[y] = plane
end

puts map

File.write("../../../../assets/maps/tmp.map", TerrainMap.encode(map))
