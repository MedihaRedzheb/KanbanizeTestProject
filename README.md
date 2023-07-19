*Execute all tests:*
mvn test

*Execute all tests from specific test class:*
mvn test -Dtest=AccountTests

*Execute single test from specific test class:*
mvn test -Dtest=AccountTests#createNewUser

*Path to html report -->* \target\surefire-reports\emailable-report.html