import paramiko, socket, threading
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("120.26.50.140", 22, "root", "Root@123", timeout=10)
print("SSH connected")
def forward(src, dst):
    try:
        while True:
            data = src.recv(4096)
            if not data: break
            dst.sendall(data)
    except: pass
    finally:
        try: src.close()
        except: pass
        try: dst.close()
        except: pass
def handle(client):
    try:
        chan = ssh.get_transport().open_channel("direct-tcpip", ("127.0.0.1", 15432), client.getpeername())
        if chan is None: client.close(); return
        t1 = threading.Thread(target=forward, args=(client, chan), daemon=True)
        t2 = threading.Thread(target=forward, args=(chan, client), daemon=True)
        t1.start(); t2.start()
        t1.join(); t2.join()
    except: client.close()
server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server.bind(("127.0.0.1", 15432))
server.listen(10)
print("Tunnel on 15432")
while True:
    client, addr = server.accept()
    threading.Thread(target=handle, args=(client,), daemon=True).start()