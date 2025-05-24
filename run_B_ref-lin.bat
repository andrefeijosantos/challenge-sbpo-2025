@echo off
set t=0

for /L %%l in (1,1,5) do (
    for /L %%i in (1,1,9) do (
        if not exist "logs/ref-lin_B_000%%i_%%l.txt" (
            set t=%%l
            goto :breakLoop
        )
    )

    for /L %%i in (10,1,20) do (
        if not exist "logs/ref-lin_B_00%%i_%%l.txt" (
            set t=%%l
            goto :breakLoop
        )
    )
)

:breakLoop
@echo on

java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/b/instance_0001.txt test_out.txt > logs/ref-lin_B_0001_%t%.txt
java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/b/instance_0002.txt test_out.txt > logs/ref-lin_B_0002_%t%.txt
java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/b/instance_0003.txt test_out.txt > logs/ref-lin_B_0003_%t%.txt
java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/b/instance_0004.txt test_out.txt > logs/ref-lin_B_0004_%t%.txt
java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/b/instance_0005.txt test_out.txt > logs/ref-lin_B_0005_%t%.txt
java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/b/instance_0006.txt test_out.txt > logs/ref-lin_B_0006_%t%.txt
java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/b/instance_0007.txt test_out.txt > logs/ref-lin_B_0007_%t%.txt
java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/b/instance_0008.txt test_out.txt > logs/ref-lin_B_0008_%t%.txt
java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/b/instance_0009.txt test_out.txt > logs/ref-lin_B_0009_%t%.txt
java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/b/instance_0010.txt test_out.txt > logs/ref-lin_B_0010_%t%.txt

java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/b/instance_0011.txt test_out.txt > logs/ref-lin_B_0011_%t%.txt
java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/b/instance_0012.txt test_out.txt > logs/ref-lin_B_0012_%t%.txt
java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/b/instance_0013.txt test_out.txt > logs/ref-lin_B_0013_%t%.txt
java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/b/instance_0014.txt test_out.txt > logs/ref-lin_B_0014_%t%.txt
java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/b/instance_0015.txt test_out.txt > logs/ref-lin_B_0015_%t%.txt

PAUSE