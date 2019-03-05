import random

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D


def cal_distance(vec1, vec2):
    return np.sqrt(np.sum(np.square(np.array(vec2) - np.array(vec1))))


def load_data(flag: int):
    if flag != 0:
        dataset = pd.read_csv('dataSet.csv', header=None, sep=',')
    else:
        # 给读入数据的class编号
        names = ['sepal-length', 'sepal-width', 'petal-length', 'petal-width', 'classes']
        dataset = pd.read_csv('iris.csv', header=None, sep=',', names=names)
        dataset.loc[dataset.classes == 'Iris-setosa', 'classes'] = 0
        dataset.loc[dataset.classes == 'Iris-versicolor', 'classes'] = 1
        dataset.loc[dataset.classes == 'Iris-virginica', 'classes'] = 2
    # dataset.drop(['classes'], axis=1, inplace=True, errors='ignore')
    return dataset.values.tolist()


class KMeans:

    def __init__(self, k, dataset=None):
        # 读取数据集
        self.dataset = load_data(dataset if dataset else 0)
        # 初始设置k个簇类
        self.k = k
        # 保存本次遍历中的质心
        self.centroids = random.sample(self.dataset, self.k)
        # 保存本次遍历中的簇
        self.clusters = dict()
        # 遍历次数
        self.times = 1

    # 开始聚类
    def clustering(self):
        self.clusters.clear()
        for item in self.dataset:
            vec1 = item
            index = -1
            min_distance = float('inf')
            for i in range(self.k):
                vec2 = self.centroids[i]
                temp_distance = cal_distance(vec1, vec2)

                if temp_distance < min_distance:
                    min_distance = temp_distance
                    index = i
            if index not in self.clusters.keys():
                self.clusters[index] = []
            self.clusters[index].append(item)

    # 更新质心
    def cal_centroids(self):
        self.centroids.clear()
        for key in self.clusters.keys():
            self.centroids.append(np.mean(self.clusters[key], axis=0).tolist())

    # 计算误差
    def cal_cost(self):
        sum = 0.0
        for key in self.clusters.keys():
            vec1 = self.centroids[key]
            temp_distance = 0.0
            for item in self.clusters[key]:
                vec2 = item
                temp_distance += cal_distance(vec1, vec2)
            sum += temp_distance
        return sum

    def show_cluster(self):
        color = ['r', 'b', 'g', 'k', 'y', 'w']  # 不同簇类标记，o表示圆形，另一个表示颜色
        centroid_marker = ['dr', 'db', 'dg', 'dk', 'dy', 'dw']

        if len(self.centroids[0]) == 2:
            for key in self.clusters.keys():
                plt.plot(self.centroids[key][0], self.centroids[key][1], centroid_marker[key], markersize=12)
                for item in self.clusters[key]:
                    plt.plot(item[0], item[1], 'o' + color[key])
        else:
            fig = plt.figure()
            ax = fig.add_subplot(111, projection='3d')
            for key in self.clusters.keys():
                ax.scatter3D(self.centroids[key][0], self.centroids[key][1], self.centroids[key][2], marker='X', c=color[key], s=50)
                for item in self.clusters[key]:
                    ax.scatter3D(item[0], item[1], item[2], marker='.', c=color[key])
        plt.show()

    def print_info(self, cost):
        print('times:', str(self.times), 'cost:', cost, 'centroids', self.centroids)
        self.times += 1
        self.show_cluster()

    def train(self):
        self.clustering()
        pres = self.cal_cost()
        prev = 1
        self.print_info(pres)

        while abs(pres - prev) >= .00001:
            self.cal_centroids()
            self.clustering()
            prev = pres
            pres = self.cal_cost()
            self.print_info(pres)


'''
当选取dataset0，即iris.csv，建议设置k为3；
当选取dataset1，即dataSet.csv，建议设置k为4
'''
k_means = KMeans(3, dataset=0)
k_means.train()

