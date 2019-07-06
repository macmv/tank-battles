
fbx_files =  Dir.glob("../../../../assets/**/*.fbx")

p fbx_files

fbx_files.each do |fbx|
  `fbx-conv #{fbx}`
end
