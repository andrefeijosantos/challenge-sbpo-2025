for /l %%x in (1, 1, 9) do (
   java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/a/instance_000%%x.txt out_file.txt
)