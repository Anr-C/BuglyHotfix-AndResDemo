#!/usr/bin/python
# coding: utf8
import sys, os
import subprocess
import getopt
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


def release_patch(old_path):
    if platform.system() == "Windows":
        cmd_head = "gradlew.bat -Pcommand"
    else:
        cmd_head = "./gradlew -Pcommand"
    if new_apk == '':
        patch_cmd = cmd_head + ' app:initDir -PmultiApkDir=' + old_path + ' app:buildTinkerPatchRelease'  # --info
    else:
        patch_cmd = cmd_head + ' app:initDir -PmultiApkDir=' + old_path + ' -PnewApkPath=' + new_apk + ' app:buildTinkerPatchRelease'  # --info

    print("patch_cmd: "+patch_cmd)
    
    p = subprocess.Popen(patch_cmd, shell=(platform.system() != "Windows"), stdout=subprocess.PIPE,
                         stderr=subprocess.STDOUT)
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


# 用于匹配文件
def fetch_file():
    print('multiApkDir: ' + multi_apk_dir)
    g = os.listdir(r"" + multi_apk_dir + "")
    for base_name in g:
        apk = os.path.join(multi_apk_dir, base_name)
        if not os.path.isdir(apk): continue
        if base_name == 'patch': continue
        print('base_name:==>' + base_name)
        print('new_apk:==>' + new_apk)
        # 制作补丁包
        release_patch(apk)
    return


if __name__ == "__main__":
    get_prop(sys.argv[1:])
    fetch_file()
