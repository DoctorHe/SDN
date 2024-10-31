import joblib
import random
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
import lightgbm as lgb
from lightgbm import LGBMClassifier


def GetData(dir='../data/collect_data.log'):
    data = []
    target = []

    with open(dir, 'r') as f:
        d = f.readline().strip()
        while d:
            array_data = d.split()[2:]
            line = [float(i) for i in array_data]
            label = line[-1]
            dd = line[:2] + line[3:-1]
            #dd = line[:-1]
            data.append(dd)
            target.append(label)
            d = f.readline().strip()

    return np.array(data), np.array(target)
    


X,y = GetData()
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=2022) # 数据集分割


gbm = lgb.LGBMClassifier(learning_rate = 0.01, n_estimators=49)
#gbm = lgb.LGBMClassifier(learning_rate = 0.01)
gbm.fit(X_train, y_train,
        eval_set=[(X_test, y_test)],
        eval_metric='binary_logloss',
        callbacks=[lgb.early_stopping(5)])
#eval_metric默认值：LGBMRegressor 为“l2”，LGBMClassifier 为“logloss”，LGBMRanker 为“ndcg”。
#使用binary_logloss或者logloss准确率都是一样的。默认logloss
y_pred = gbm.predict(X_test)
# 计算准确率
accuracy = accuracy_score(y_test,y_pred)
print("accuarcy: %.2f%%" % (accuracy*100.0))
joblib.dump(gbm, './lightGBM.pkl')
