import sys
import pandas as pd

from statistics import mean

# Instance set name generator.
model     = sys.argv[1]
dataset   = sys.argv[2]
execs     = int(sys.argv[3])

if dataset == "A":
    instances = ["000" + str(i) for i in range(1, 10)] + ["00" + str(i) for i in range(10, 21)]
else:
    instances = ["000" + str(i) for i in range(1, 10)] + ["00" + str(i) for i in range(10, 16)]

# .csv file to save dataframe
file = sys.argv[4]

# Add new row to DataFrame.
def add_row(df, instance, solution, gap, time, hasc, hdesc):
    if model != "ref-lin":
        new_row = {"instance": instance, "solution": solution, "gap": gap, "time": time, "hasc": hasc, "hdesc": hdesc}
    else:
        new_row = {"instance": instance, "solution": solution, "gap": gap, "time": time}

    df = pd.concat([df, pd.DataFrame([new_row])], ignore_index=True)
    return df

# Get information from logs
def get_data(logs):
    objs, times = [], []
    hasc, hdesc = "-", "-"

    for log in logs:
        with open(log, 'r') as f:
            for line in f:
                if "Solution found:" in line:
                    objs.append(float(line.strip().split(":")[1].strip()))
                elif "Ascending Thread finished at: " in line:
                    hasc = int(line.strip().split(":")[1].strip())
                elif "Decending Thread finished at: " in line:
                    hdesc = int(line.strip().split(":")[1].strip())
                elif "Proved optimal? true" in line:
                    hasc = hdesc = "-"
                elif line.startswith("Time:"):
                    time_str = line.strip().split("Time:")[1].strip()
                    h, m, s = time_str.split(":")
                    v = int(h) * 3600 + int(m) * 60 + float(s)
                    times.append(v)
                    assert v <= 610

    return mean(objs), 0, mean(times), hasc, hdesc

if __name__ == "__main__":
    # Initialize DataFrame
    data = {
        "instance": [],
        "solution": [],
        "time": []
    }
    df = pd.DataFrame(data)

    for i in range(len(instances)):
        names = [model + "_" + dataset + "_" + instances[i] + "_" + str(t) + ".txt" for t in range(1, execs+1)]
        obj, gap, time, hasc, hdesc = get_data(names)
        df = add_row(df, instances[i], round(obj, 6), gap, int(time), hasc, hdesc)
        print("Instances " + str(names) + " successfully added to table")

    df.to_csv(file, index=False)