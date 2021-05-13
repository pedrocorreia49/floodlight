import subprocess
import threading
import time
import json
import requests

controller_ip_port = '192.168.56.1:8080'

def getThroughput(pool_id, id, ip):
    p = subprocess.Popen('iperf3 -c ' + ip + ' -p 500 -w 500 k -J --connect-timeout 10000', stdout=subprocess.PIPE, stderr=subprocess.STDOUT, shell=True)
    stdout, stderr = p.communicate()
    if(stderr):
        print(stderr.decode('utf-8'))
    else:
        output = json.loads(stdout)
        if(len(output['end']) == 0):
            # print(pool_id, ip, 'OFF')
            obj = {"id": id, "pool_id": pool_id, "throughput": '-1'}
            r = requests.put('http://'+controller_ip_port+'/quantum/v1.0/members/'+id+'/', json = obj)
        else:
            for i in output['end']['streams']:
                obj = {"id": id, "pool_id": pool_id, "throughput": i['sender']['bits_per_second']}
                r = requests.put('http://'+controller_ip_port+'/quantum/v1.0/members/'+id+'/', json = obj)
                # print('http://'+controller_ip_port+'/quantum/v1.0/members/'+id+'/', obj)
    
    return

def handlePool(pool_id):
    threads = list()

    r = requests.get('http://' + controller_ip_port + '/quantum/v1.0/pools/' + pool_id + '/members/')
    members = json.loads(r.text)
    for member in members:
        id = member['id']
        ip = member['address']

        x = threading.Thread(target=getThroughput, args=(pool_id, id, ip, ))
        threads.append(x)
        x.start()

    for thread in threads:
        thread.join()

    return

if __name__ == "__main__":
    threadsP = list()

    while True:
        threadsP.clear()
        r = requests.get('http://' + controller_ip_port + '/quantum/v1.0/pools/')
        pools = json.loads(r.text)
        for pool in pools:
            pool_id = pool['id']
            if(pool['lbMethod'] == 'LTP'):
                x = threading.Thread(target=handlePool, args=(pool_id, ))
                threadsP.append(x)
                x.start()

        for thread in threadsP:
            thread.join()

        print('Tests done, waiting...\n')
        time.sleep(20)