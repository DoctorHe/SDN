import socket
import json
import numpy as np
import joblib


def predict_attack(data_list):
    #data = np.array(data_list[0:6]).reshape(1, -1)
    data = np.array(data_list[0:4]).reshape(1, -1)
    print(data)
    model = joblib.load("./model/lightGBM.pkl")
    #model = joblib.load("./model/svm.pkl")
    result_nparray = model.predict(data)
    result = bool(result_nparray[0])
    # 预测结果 0是False，1是Ture
    print("预测结果", result)
    return result


def process_data(json_data):
    attack = False
    # process
    data_list = list(json_data.values())
    #data_list = data_list[3:]
    data_list = data_list[3:5] + data_list[6:]
    if predict_attack(data_list):
        attack = True
        json_data["attack"] = True
    modified_data = json_data
    return modified_data, attack


def handle_client(client_socket):
    received_data = client_socket.recv(1024)
    if received_data:
        # 解码JSON数据
        msg = ''
        msg += received_data.decode("utf-8")
        # msg是解码后的数据
        # print(msg)
        # json_data是json数据
        json_data = json.loads(msg)
        #print("original message : ", json_data)  # {'attack': False,'id': ,....}字典键值对
        modified_data, is_attack = process_data(json_data)
        print("send message : ", modified_data)
        response_data = json.dumps(modified_data)
        client_socket.send(("%s\n" % response_data).encode("utf-8"))


def main():
    # creat server socket
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # 将服务器套接字绑定到特定的主机和端口
    server_socket.bind(('localhost', 13131))
    # 监听传入的连接，允许最大排队数为1
    server_socket.listen(1)

    myaddr = server_socket.getsockname()
    print("client is : %s" % str(myaddr))

    while True:
        # 接受传入的客户端连接，并获取客户端套接字和地址
        client_socket, address = server_socket.accept()
        # address是元组，第一个是clientIP，第二个是端口
        # 增加判断，不是127.0.0.1的话报异常
        while True:
            handle_client(client_socket)


if __name__ == '__main__':
    main()
