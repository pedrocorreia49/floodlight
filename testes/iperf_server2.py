import subprocess

if __name__ == "__main__":
    with open('iperf_output2.csv', mode='w') as file:
        p = subprocess.Popen('sudo iperf -s -p 80 -y C', stdout=file, stderr=subprocess.STDOUT, shell=True)
        stdout, stderr = p.communicate()