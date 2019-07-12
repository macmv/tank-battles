
fbx_files =  Dir.glob("./**/*.fbx")

p fbx_files

fbx_files.each do |fbx|
  `fbx-conv #{fbx}`
end
