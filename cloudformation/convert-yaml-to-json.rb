#!/usr/bin/env ruby

require 'json'
require 'yaml'

if ARGV.size != 1
  puts "usage: convert.rb <file.yml>"
  exit(1)
end

input_filename = ARGV[0]
output_filename = input_filename.sub(/(yml|yaml)$/, 'json')

input_file = File.open(input_filename, 'r')
input_yml = input_file.read
input_file.close

output_json = JSON.pretty_generate(YAML::load(input_yml))

output_file = File.open(output_filename, 'w+')
output_file.write(output_json)
output_file.close
