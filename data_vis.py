import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import os 


filename = os.path.join(os.path.dirname(os.getcwd()), "b_verify-merkle-prefix-trie/benchmark-results.csv")
data = pd.read_csv(filename)


def plot_path_lengths():
    x   = np.log10(data['updates'])
    y_update_path = data['update_path_avg']
    y_full_path = data['full_path_avg']
    full_plt_path, = plt.plot(x, y_full_path, 'bs', label="Full Path")
    update_plt_path,  = plt.plot(x, y_update_path, 'r--', label="Updates Only")
    plt.ylabel("Proof Size in Bytes")
    plt.xlabel("Log (base 10) of Number of Updates")
    plt.title("Average Proof Sizes for a MPT Dictionary with 10^6 Entries")
    plt.legend(handles=[full_plt_path, update_plt_path], loc=4)
    plt.show()


def plot_hashes_required_in_update():
    x   = data['updates'] / data['n']
    total = data['nInteriorNode']+data['nEmptyLeaf']+data['nNonEmptyLeaf']
    hashes_updated = data['nHashesToCommit']
    y = hashes_updated / total
    a, = plt.plot(x, y, 'ro', linestyle='-', label="fraction required to update")
    b, = plt.plot(x, x, linestyle='-', label="line y = x for comparison")
    plt.ylabel("Fraction of Hashes That Must Be Recalculated")
    plt.xlabel("Fraction of Nodes Updates")
    plt.title("Computation Required to Update a MPT Dictionary with 10^6 Entries")
    plt.legend(handles=[a, b], loc=4)
    plt.show()
    
def get_stats():
    interior = data['nInteriorNode'][0]
    empty = data['nEmptyLeaf'][0]
    nonempty = data['nNonEmptyLeaf'][0]
    total = interior + empty + nonempty
    print("TOTAL NODES: "+str(total))
    print("INTERIOR NODES: "+str(interior)+" ("+str((interior/total) * 100)+" %)")
    print("EMPTY LEAF NODES: "+str(empty)+" ("+str((empty/total) * 100)+" %)")
    print("NONEMPTY LEAF NODES: "+str(nonempty)+" ("+str((nonempty/total) * 100)+" %)")
    
    
plot_hashes_required_in_update()
    