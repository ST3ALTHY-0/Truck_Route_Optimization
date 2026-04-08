The project I want to make is based on a project I have worked on my own for a month or two. It is a web based, vehicle route optimization project where I try to optimize/minimize the number trucks that needs to be sent from a warehouse to any number of drop off points and then return to the warehouse. 
A user can enter some number of waypoints in the form of addresses i.e. (Indianapolis, Indiana : Columbus, Ohio), then enter other data and constraints, such as the number of trucks available, max number of miles any singular truck can travel, max number of hours a truck can be in use among, max capacity of each truck, and others. 
We then process the data in the backend with a few apis and libraries to ultimately get a set of routes we pass back to the frontend to display to the user.
Thus far in my project I have implemented an extremely limited MySql database solely to store addresses and their related coordinates (long, lat) in a single DB table. This was done only to limit api calls to a public api to speed up the application and avoid rate limits.
My goal in this this project is to design and implement a more complete MongoDB and MySQL database to store more of the data that the user passes to the server, and the results from the server, and to be able to analyze the data or maybe allow the user to see their own data.


Project Planning
Database Design (20pts)
[Use a few sentences to describe what you need to do for this project from database design point of view, such as the steps of designing the database entities etc.]

In my java project I already have a few data transfer objects. These are objects like Location which holds address and coordinate data, optimizationRequest which holds all the config data the user passes to the optimizer, and RouteAPIResponse which holds many objects, nested objects, and data like encodedGeometry. 
Its pretty clear to me that the objects containing deep nesting like RouteAPIResponse or objects that can change a lot like optimizationRequest should be stored in MongoDB. While simpler objects like location or objects that are interconnected but don't need heavy normalization within my java code can be stored in MySql.


Datasets in Scope (20pts)
[Identify a few existing data sets you can use to populate or initialize your databases. Often you need to make changes (such as add or remove fields etc) to the existing data sets, please describe your plan to make the changes.]


I can initialize data in my DB with sets of (String) addresses and a (Integer) pallet amount for each location. Additionally I have a csv which contains >100 locations with different combinations of locations and pallet demand which I can use to test or populate the DBs. 
The main Objects that I expect to edit fields of will almost certainly be stored in my Mongo DB, these are objects like request objects or response objects that I might change what they contain or how the data is nested. In that case, a minimal amount of changes will be needed in my code base other than actually changing the fields. For objects stored in MySql I can still edit the relationship in the java model and it should propagate to my DB automatically, however from past experience I know that you also need to edit exactly how you implement the model wherever it is referenced throughout the code, as well as deal with the null fields/other changes in the old objects in the DB, for this reason I will really try to make sure whatever I store in the SQL DB is really solid design wise so I don't have to make that many changes. 

It is my plan to mostly use JPA/Hibernate to handle the Database implementation from my java code; However, if you would like for me to use a more direct approach using JDBC and Mongo Driver to touch the DBs more directly, I am happy to do that.

Some resources for finding JSON files:
https://catalog.data.gov/dataset?res_format=JSON&_res_format_limit=0
https://www.kaggle.com/datasets?fileType=json 

