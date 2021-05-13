import subprocess

if __name__ == "__main__":
    threadsP = list()
    p = subprocess.Popen('iperf3 -s -p 500', stdout=subprocess.PIPE, stderr=subprocess.STDOUT, shell=True)
    stdout, stderr = p.communicate()
    if(stderr):
        print(stderr.decode('utf-8'))
    else:
        print(stdout.decode('utf-8'))