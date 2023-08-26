from sklearn import metrics
import sys
import math


predicted = []

with open('labels/' + sys.argv[2]) as f:
    for line in f:
        predicted.append(int(line.split()[0]))


data = []
labels = []

hasLabel = False
labelBeg = False
norm = True


with open("instance_" + sys.argv[1]) as file:
  line = file.readline()
  for flag in line.split():
    if flag == "l":
      hasLabel = True
    elif flag == "lb":
      hasLabel = True
      labelBeg = True

  n = int(file.readline().split()[0])
  f = int(file.readline().split()[0])
  if hasLabel:
    k = int(file.readline().split()[0])

  for i in range(n):
    line = file.readline()
    point = [float(x) for x in line.split()]
    if labelBeg:
      data.append(point[1:f+1])
      labels.append(point[0])
    elif hasLabel:
      data.append(point[0:f])
      labels.append(point[f])
    else:
      data.append(point)

if norm:
  maxValue = []
  minValue = []

  for j in range(f):
    maxValue.append(sys.float_info.min)
    minValue.append(sys.float_info.max)
    for i in range(n):
      if data[i][j] < minValue[j]:
        minValue[j] = data[i][j]
      if data[i][j] > maxValue[j]:
        maxValue[j] = data[i][j]
    
    if maxValue[j] > minValue[j]:
      for i in range(n):
        data[i][j] = (data[i][j]-minValue[j])/(maxValue[j]-minValue[j])*100

#wcs = 0

#for i1 in range(n):
#  for i2 in range(n):
#    if i2 <= i1:
#      continue
#    if predicted[i1] == predicted[i2]:
#      dis = 0
#      for j in range(f):
#        dis += (data[i1][j] - data[i2][j]) * (data[i1][j] - data[i2][j])
#      wcs += math.sqrt(dis)


if(hasLabel):
  print("ARI:", metrics.adjusted_rand_score(labels, predicted))
  print("RI:", metrics.rand_score(labels, predicted))
  print("NIM:", metrics.normalized_mutual_info_score(labels, predicted))


print("Silhouette:" , metrics.silhouette_score(data, predicted))
#print("WCS:" , wcs)   
