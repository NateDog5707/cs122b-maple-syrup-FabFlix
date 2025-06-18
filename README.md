PLEASE NOTE: We continued working in the project1 folder, so the project1 folder includes all files for project 4
[They hyperlinked this template and told us to use it, so I just copy pasted it in here.]
- # Project 4
    - #### Team#: maple_syrup
    
    - #### Names: Ananya Kashyap, Nathan Yiyue Yan
    
    - #### Project 4 Video Demo Link:
    - https://youtu.be/AaqAlsIhGXg
    - #### Instruction of deployment:
    - For barebones usage, you only need to launch/connect to the master.
    - For load-balancing usage, you should launch the master, slave, and load balancer. Connect to the load balancer instead.

    - #### Collaborations and Work Distribution:
    - ##### Ananya:
    - Task 1
      - EC Fuzzy Search using SOUNDEX
    - Task 2
 
    - ##### Nathan:
    - Task 3
    - Task 4


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
    - context.xml file was modified to configure the Tomcat JDBC connection pool (factory, max total connections, max idle connections, and max wait time for a caller, caching prepared statements flag in url added)
    - All java servlets that interact with the SQL server use JDBC connection pooling
      - AddMovie
      - AddStar
      - BrowseServlet
      - DashboardLoginServlet
      - FullTextSeachServlet
      - LoginServlet
      - MovieListServelt
      - MyStartupListener
      - PaymentPageServlet
      - SingleMovieServlet
      - SingleStarServlet
      - ViewMetadata
    
    - All java servlets that take user input use Prepared Statements
      - LoginServlet
      - DashboardLoginServlet
      - FullTextSearchServlet
      - AddStarServlet
      - AddMovieServlet
    
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
    - Tomcat initializes a connection pool at startup. Whenever any of the servlets call dataSource.getConnection(), it borrows an existing connection from the pool. It accesses the DB, then returns the connection to the pool. Instead of opening a connection every time a call needs to be made to the DB, pooling allows for fast and efficient access.
    - In the servlets that use connection pooling, the init function looks up the connection pool through
      ```
      dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
      ```
    - Later, when running
      ```
      try (Connection conn = dataSource.getConnection()) {
        ...
      }
      ```
      A connection is borrowed from the connection pool. When the try block exits, the connection is returned to the pool (yippe)!
    
    - #### Explain how Connection Pooling works with two backend SQL.
    - The master/slave pools separate work, the master handles any writes to the database and both master and slave may handle reads. The load balancer can switch between the two pools depending on what sql function is occuring at the moment. These two pools may work independently, reducing load and better scaleability. 
    
- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
    - #### All in the project1/src/ directory:
      - #### Master
      - AddMovie
      - AddStar
      - MovieListServlet
      - SAXParser/ActorParser
      - SAXParser/CastParser
      - SAXParser/MySAXParser
      - SAXParser/SQLInserter
      - #### Slave
      - BrowseServlet
      - FullTextSearchServlet
      - LoginServlet
      - SearchServlet
      - WelcomeServlet
      - SingleMovieServlet
      - SingleStarServlet
    - #### How read/write requests were routed to Master/Slave SQL?
    - Write requests, like insert, update, and delete, were all routed to the master. Select queries were sent to the slave.
    
# Project 3
## Video URL: https://youtu.be/XMznWrTW0YE

## Contributions
### Nathan:
- Task 6 XML Parser
  - XML Parsing inconsistency report file
    To access the report file:
    After running the program and website,
    1) Navigate to the tomcat directory
    2) Go to the /bin/logs directory
    3) See file "xmlLog.txt"
    As an example, sample_xmlLog.txt is provided in the root directory

  - Two algorithm optimizations
    1) See maple-syrup-performance-optimizations.pdf for details on two algorithm optimizations.
       
- Task 2 HTTPS

### Ananya:
- Task 1 reCAPTCHA
- Task 3 Prepared Statements
  - All files that take input from the user (including from the employee) contain PreparedStatements
    - Login Servlet
    - Employee Login Servlet
    - Search Servlet
    - Add Star Servlet
    - Add Movie Servlet
- Task 4 Encrypted Passwords
- Task 5 Employee Dashboard

# Project 2
## Video URL: https://youtu.be/lPQCtuhxw1o

## Contributions
### Nathan:
- Task 1
- Task 4
- Jump Functionality (Task 3)
- Video Recording + Uploading
 
### Ananya:
- Task 2
  - Used '%{string}% for search functionality
  - Used '{starting alphanumeric character}%' for browsing by genre and title
- Task 3
- README.md contributions

# Project 1
## Video URL: https://youtu.be/1qz49XYC61o

## Contributions
### Nathan:
- AWS instance setup
- Repository structuring and setup
  - Servlets structuring
        - Referring to the frontend/backend separation example provided
  - WebContent files structuring (.js and .html)
      - Final frontpage.html
      - Final single-movie.js
      - Final single-movie.html
      - Final movie-list.html
  - created create_table.sql file
  - created web.xml
  - created pom.xml
  - created context.xml
- Recorded final video and uploaded to YouTube
  
### Ananya:
  - Added/Amended create_table.sql file
  - SQL queries and JSON Object and Array creations for Servlets
    - MovieListServlet.java
    - SingleMovieServlet.java
    - SingleStarServlet.java
  - WebContent
    - Partial (amended issues) movie-list.js
    - Final single-star.js
    - Final single-star.html
  - Added to pom.xml
  - Contributions README
