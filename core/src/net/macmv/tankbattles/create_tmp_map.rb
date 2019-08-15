
# ************************
# Run from this directory!
# ************************

# mkdir and build proto for ruby, such that I can easily make a script to generate a map
# this needs ./gradelw installDist to be called first

`mkdir -p ../../../../build/generated/source/proto/ruby/`
`protoc ./lib/proto/mainProto.proto --ruby_out ../../../../build/generated/source/proto/ruby/`

require_relative "../../../../build/generated/source/proto/ruby/lib/proto/mainProto_pb.rb"

width = 10
max_height = 0.5 # is 0 to max_height
length = 20

map = TerrainMap.new
map.width = width
map.length = length

width.times do |x|
  row = TerrainMap::Row.new
  length.times do |z|
    point = TerrainMap::Point.new
    pos = Point3.new(x: x, y: rand * max_height, z: z)
    point.type = TerrainMap::Point::Type::GRASS
    point.pos = pos
    row.points[z] = point
  end
  map.rows[x] = row
end

puts map

File.write("../../../../assets/maps/tmp.map", TerrainMap.encode(map))
