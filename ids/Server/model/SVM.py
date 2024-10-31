from sklearn.svm import SVC
import joblib
import random


def GetData(dir='../data/collect_data.log'):
    data0 = []
    data1 = []
    label0 = []
    label1 = []
    with open(dir, 'r') as f:
        d = f.readline().strip()
        while d:
            array_data = d.split()[2:]
            line = [float(i) for i in array_data]
            label = line[-1]
            dd = line[:2] + line[3:-1]
            #dd = line[:-1]
            if label == 0:
                data0.append(dd)
                label0.append(label)
            else:
                data1.append(dd)
                label1.append(label)
            d = f.readline().strip()

    random.shuffle(data1)
    random.shuffle(data0)
    c0 = int(len(data0) * 2 / 3)
    c1 = int(len(data1) * 2 / 3)
    train_data = data0[:c0] + data1[:c1]
    test_data = data0[c0:] + data1[:c1]
    train_label = label0[:c0] + label1[:c1]
    test_label = label0[c0:] + label1[c1:]

    train = list(zip(train_data, train_label))
    random.shuffle(train)
    train_data, train_label = zip(*train)
    test = list(zip(test_data, test_label))
    random.shuffle(test)
    test_data, test_label = zip(*test)
    return train_data, train_label, test_data, test_label


def GetAcc(pre_y, test_label):
    acc = 0.
    for i in range(len(test_label)):
        if pre_y[i] >= 0.5:
            pre_y[i] = 1
        else:
            pre_y[i] = 0
        if pre_y[i] == test_label[i]:
            acc += 1
    print('accuracy is:', acc / len(test_label))


def classification(train_data, train_label, test_data, test_label):
    model = SVC(C=0.6, kernel='linear')
    model.fit(train_data, train_label)
    joblib.dump(model, './svm.pkl')
    pre_y0 = model.predict(train_data)
    pre_y1 = model.predict(test_data)
    GetAcc(pre_y0, train_label)
    GetAcc(pre_y1, test_label)


if __name__ == '__main__':
    train_data, train_label, test_data, test_label = GetData()
    classification(train_data, train_label, test_data, test_label)
