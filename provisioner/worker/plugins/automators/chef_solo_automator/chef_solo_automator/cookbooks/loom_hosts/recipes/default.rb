#
# Cookbook Name:: loom_hosts
# Recipe:: default
#
# Copyright 2013-2014, Continuuity, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

node['loom']['cluster']['nodes'].each do |n, v|
  short_host = v.hostname.split('.').first
  next unless v.key?('ipaddresses') && v['ipaddresses'].key?('bind_v4') && v['ipaddresses'].key?('access_v4')
  # Hosts entry for bind_v4 goes before access_v4, so bind_v4 resolves first
  hostsfile_entry v['ipaddresses']['bind_v4'] do
    hostname v.hostname
    aliases [ short_host ]
    unique true
    action :create
  end
  hostsfile_entry v['ipaddresses']['access_v4'] do
    hostname v.hostname
    aliases [ short_host ]
    unique true
    action :create
  end
end
