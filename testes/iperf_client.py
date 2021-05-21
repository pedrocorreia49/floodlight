import subprocess
import time 
import sys
if __name__ == "__main__":
    ip = '10.0.0.100'
    for i in range(0,30):
        p = subprocess.Popen('sudo iperf -c ' + ip + ' -p 80 -y C -P 20', stdout=subprocess.PIPE, stderr=subprocess.STDOUT, shell=True)
        time.sleep(6)
    stdout, stderr = p.communicate()
