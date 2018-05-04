import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import os 


filename = os.path.join(os.path.dirname(os.getcwd()), "b_verify-merkle-prefix-trie/benchmark-results.csv")
data = pd.read_csv(filename)

x   = data['updates']/data['n']


y_update_path   = data['update_path_avg']
y_full_path = data['full_path_avg']
full_plt_path, = plt.plot(x, y_full_path, 'bs', label="Full Path")
update_plt_path,  = plt.plot(x, y_update_path, 'r--', label="Updates Only")
plt.ylabel("Proof Size in Bytes")
plt.xlabel("Fraction of Values Updated")
plt.title("Average Proof Sizes for a MPT Dictionary with 10^5 Entries")
plt.legend(handles=[full_plt_path, update_plt_path], loc=4)


plt.show()