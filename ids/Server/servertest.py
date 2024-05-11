import socket

HOST = 'localhost'
PORT = 8010


def main():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, PORT))
        s.listen()
        print(f"Listening on {HOST}:{PORT}")
        while True:
            conn, addr = s.accept();
            with conn:
                print('Connect by', conn)
                data = conn.recv(1024)
                if not data:
                    break
                result = process_data(data.decode('utf-8'))
                conn.sendall(result.encode('utf-8'))


def process_data(data):
    print(data)
    return "yu ce jie guo"


if __name__ == '__main__':
    main()
