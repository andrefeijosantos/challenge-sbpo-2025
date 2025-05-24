set arg1=%1
set arg2=%2
set arg3=%3
set arg4=%4

java -Djava.library.path=C:/Applications/CPLEX_Studio2211/opl/bin/x64_win64 -jar target/ChallengeSBPO2025-1.0.jar datasets/%arg2%/instance_%arg1%.txt test_out.txt > logs/param-alg_%arg3%_%arg1%_%arg4%.txt