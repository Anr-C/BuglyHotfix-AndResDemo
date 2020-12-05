#!/usr/bin/python
# coding: utf8
import sys, os
import subprocess
import getopt
import shutil
import platform

multi_apk_dir = ''
new_apk = ''

def get_prop(argv):
    try:
        opts, args = getopt.getopt(argv, "hd:n:", ["multiApkDir=", "newApk="])
    except getopt.GetoptError:
        print('tinker-patch.py -d <multiApkDir> -n <newApk>')
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print('tinker-patch.py -d <multiApkDir> -n <newApk>')
            sys.exit()
        elif opt in ("-d", "--multiApkDir"):
            global multi_apk_dir
            multi_apk_dir = arg
        elif opt in ("-n", "--newApk"):
            global new_apk
            new_apk = arg
    return

def release_patch(mapping, old_path, new_path):
    # write mapping
    f = open('tinker_proguard.pro', 'r+')
    proguard_f = f.readlines()
    proguard_f[0] = '-applymapping ' + mapping + ' \n'
    f = open('tinker_proguard.pro', 'w+')
    f.writelines(proguard_f)

    patch_cmd = 'java -jar ./tinker-patch-cli-1.9.14.9-all.jar -old ' + old_path + ' -new ' + new_path + ' -config tinker_config.xml -out out'
    p = subprocess.Popen(patch_cmd, shell=(platform.system() != "Windows"), stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    while p.poll() is None:
        line = p.stdout.readline()
        line = line.strip()
        if line:
            print(line)
    if p.returncode == 0:
        print('Subprogram success')
    else:
        print('Subprogram failed')
    return

#用于复制补丁
def copy_patch(base_name):
    if platform.system() == "Windows":
        patch_path = os.getcwd() + '/out/patch_signed_7zip.apk'
    else:
        patch_path = os.getcwd() + '/out/patch_signed.apk'
    out_patch_dir = os.getcwd() + '/patch/' + base_name
    out_patch = out_patch_dir + '/patch_signed_7zip.apk'
    if not os.path.exists(out_patch_dir):
        os.makedirs(out_patch_dir)
    shutil.copyfile(patch_path, out_patch)
    return

#用于匹配文件
def fetch_file():
    print('multiApkDir: ' + multi_apk_dir)
    g = os.listdir(r"" + multi_apk_dir + "")
    for base_name in g:
        apk = os.path.join(multi_apk_dir, base_name)
        if not os.path.isdir(apk): continue
        print('base_name:==>' + base_name)
        mapping = apk + '/mapping.txt'
        print('mapping:==>' + mapping)
        old_path = apk + '/' + base_name + '-resguard.apk'
        print('old_path:==>' + old_path)
        print('newApk:==>', new_apk)
        # 制作补丁包
        release_patch(mapping, old_path, new_apk)
        # 拷贝补丁包
        copy_patch(base_name)
    return

if __name__ == "__main__":
    get_prop(sys.argv[1:])
    fetch_file()

# for xml if need
# import xml.etree.ElementTree as ET
# updateTree = ET.parse("tinker_config.xml")   # 读取待修改文件
# root = updateTree.getroot()
#
# sub1 = root.find("sub1")            # 修改sub1的name属性
# sub1.set("name","New Name")
#
# updateTree.write("tinker_config.xml")        # 写回原文件