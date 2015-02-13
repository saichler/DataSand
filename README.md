# DataSand
Data Sand is an open source project built to be a toolkit for the "Internet of Everything" and analytics, aiming to make it ridiculously simple & easy to build, store, share & query applications/services/data using the “Social Networking” approach.

## Internet Of Everything
The Internet Of Everything suggests that everything is connected to the network, from the core router in the carriers network to the Microwave & Fridge @ home. Each connected element has some functionality, probably has some data to store, would probably need some management & might need to share & interact with other elements on its network that have some functional commonality.

### Home Sequence Example
Let’s say you have your bathroom heater and your coffee machine connected to the smart home and you want your bathroom heater to be turned on once you enter the bathroom and turned off once you leave the bathroom. At the same point when you leave the bathroom you wish for the coffee machine to start brewing so you will have hot coffee ready once you get down. 

####Traditionally
You will have a “Command Center” where both the bathroom heater & the coffee machine are connected to so the sequence of operations would be: BTH message CC on enter->CC message BHT to turn on->BTH message CC on exist->CC message BTH to turn off->CC message coffee maker to turn on.

####Data Sand way
BTH Turn On on Enter->BTH Turn Off on Exit->BTH ARP Finish to BTH Group Listeners->Coffee machine receive ARP and turned on. As you can see, the Command Center isn’t part of the elements interaction, hence the “Social Networking” as the different elements interact with each other. The Command Center is still there, however it;s job is to only configure the sequence initially.


### The 200% & 10 Million Guidelines 
* "Will optimizing a module or an application by 200% will satisfy the requirements?"
* "Can it scale to 10M?"
Those questions helped logically challenge the way a module or an application is designed and were guidelines in designing Data Sand.

### Traditionally
Traditionally your application would pull the data from the source (or the source will push it)->parse the data (if needed)->abstract into a model (even if it is just POJOs)-> Store it in a data store for doing SDN logic or for Management & etc. This creates the "Big Data" challenge... Many have created wonderful data stores that are extremely scaleable, but then again, out of the top of my mind, you have the following challenges:
* How do you query the data?
* What part of the data needs to be indexed? 
* How fast would it be to retrieve it?
* What and how much hardware do you need to host it? 
* How much compute power do you need?
* How do you configure your store for your model (e.g. annotating the POJOs like Hibernate)?
* & possibly more considerations...

## Idea behind Data Sand
Maybe it is far-fetched, although in a nutshell, the Data is already there, "Sharded" in the network so we just need an infrastructure to collect and unify it through a single interface with a convenient NBI of SQL & JDBC that will Virtualize the sharded data into one big SQL Data Base.

* Node Networking
  Provide a transparent as possible way for a node to join a Network Of Nodes, allowing communication between JVMs regardless of their process/machine location.
* ARPing Groups
  Provide an infrastructure to ARP data between nodes registered for groups/subject so one node can share data with its adjacents without the need to "know" their location.
* 10M Nodes
  Provide a similar mechanism to OSFP aggregator, one node will aggregate the data from a group of nodes.
* High Availability
  The agggregator node is already doing that... E.g. it aggregate the data into a common place, that is also a Data Sand node, hence it store it in some persistence layer. 
  For example, if the data is in memory on the edge nodes, the aggregator store the data in HBase or similar...
* What data store?
  Provide a simple mediation layer that allow you to easily implement and plug-in any kind of data store. For example, in an edge node one might need an in memory data store while @ the 
  aggregator one will want SQL or NoSQL data stores, but again, allow to query both with the same SQL language.
* POJO Persisting Layer
  Provide a Serialization Layer to translate POJOs into a data store format (e.g. bytes, Statement or whatever) and back.
* SQL & JDBC
  Provide an SQL & JDBC interface to query the data regardless of its location and persistence method.
* Data Throttling
  Sometimes it important to throttle the data stream as it does not matter how fast data is being collected, the consumer cannot process it fast enough. Data Sand implements a very basic
  protocol between the client and the Data Providing Nodes to throttle and make sure none of the sides get overflowed with memory consumption.

# Deep Dive into Data Sand

Data sand is build out of 3 components: Node Networking, Object Serialization, Object Store.

## Data Sand Virtual JVM Network
[[File:DataSand-Network.png|thumbnail|Network]]
Byte Serialization layer is integrated into DataSand so it can share Objects between JVMs. Now being part of the Networking Hardware business, why not create a Virtual Network Switching between the JVM processes to make it easy to share info between them? I must declare that this idea isn't mine, however i have simplified it a lot by defining that the switching will be done only based on one "hope" scenarios, hence i do not need to implement a "Spanning Tree" protocol to block loops as i do not have loops. A question popup from the last statement, "What do we do if there isn't any direct connectivity between all the machines in the setup?" (e.g. devices can be on different subnets with no connectivity between them). When that happens, Data Sand will just create a "Tunnel" through one of the nodes that has connectivity to each of the machines and the tunnel will be considered as one hope.

### NetworkNode
A NetworkNode is an Object that once you instantiate it, it will bind to the first port that is available starting from port 50000, e.g. it will try to bind to port 50000 then 50001...50002 & so on until success. The first NetworkNode that binds to port 50000 is taking the role of the Machine jvSwitch and any NetworkNode that binds to port 50001 and up will automatically try to create a connection to the NetworkNode on port 50000, hence the JVMs in a single machine have a "Star Topology" communication inside the machine. For example say NetworkNode that bind to port 50004 sends a message to the NetworkNode that bind to port 50007, the message will be sent to NetworkNode @ port 50000 and it will switch it to port 50007. The big idea is that each NetworkNode can implement ARP broadcast according to specific subjects and "discover" its peers without knowing up front which one implements the business logic of the ARP group. 

A connection between two machines will be done only between the two NetworkNodes bind to port 50000 on each machine so for example, if NetworkNode bind to port 50004 on Machine 1 sends an ARP message-> the message will be sent to NetworkNode on port 50000 on Machine 1 -> NetworkNode 50000 will distribute the message to all NetworkNode on port 50001 and up on Machine 1-> NetworkNode 50000 on Machine 1 will send the message to NetworkNode 50000 on Machine 2-> NetworkNode 50000 on Machine 2 will distribute it to all NetworkNode 50001 and up on Machine 2.

### Infrastructure For Application Communication
Applications can utilize the JVM Networking Infrastructure of Data Sand without utilizing the the Storage capabilities, for example an App can be running on the device and collect data on-demand-> abstract it and send it over the wire to another node without actually storing it, which enable also an on-demand, real time kind of implementation of a data store without actually storing anything...

## Tree Model & Object Serialization

### Tree Model Analysis
The Idea behind Data Sand is that every Tree Model can be queried by a SQL statement, the parent-2-child relation can be referred to as one-2-many and also be automatically tagged as "Inner Join" between the parent and the child, hence for example if we have the following Pojo Tree Model:
  CEO
   |
    - EVP 1
       |
        - SVP 1
           |
            - VP 1
           |
            - VP 2
       |
        - SVP 2
        .
        .
        .
       | 
        - SVP 3
        .
        .
        .
    - EVP 2
     .
     .
     . 

We can tag each node type as a Virtual Table, so in our example we have the following VTables: CEO, EVP, SVP & VP. Now we can also define a one-2-many relation between the Vtables as follows: CEO-2-EVP, EVP-2-SVP, SVP-2-VP. Naturally, the parent-child relation also apply to parent-grand child relation so CEO is one-2-many for SVP via the CEO-2-EVP relation. Now that we have this analysis of our POJO model, we can translate SQL queries to query this POJO Tree Model as follows:

If we have the following query: "Select VP.Name,EVP.Name from VP,EVP;" based on the analysis above, the "behind the scene" meaning is "seek out all VPs inner join SVP on VP.SVP Name=SVP.Name, inner join SVP.EVP Name=EVP.Name", however the parent-2-child relation of the Tree Model enforce those inner joins so no need to specify them in the query.

Data Sand does this analysis on each object type that it is being introduce with. Data Sand comes with default rules to analyze the Objects and by default a "child" in the model is every "Getter" in the Object that returns an array or collection that its component type isn't some java native type. The different rules are flexible so if the default rules does not fit your model, you can change, extend and add more rules based on your model requirements.

### Serializing An Object
With the Analysis above, Data Sand determinate what is considered a "Child" in the Tree Model and what is considered an "Attribute". If an Object isn't a native Java type, Data sand will create a Serializer for it regardless if it is considered a "Child" or an "Attribute". A Serializer is just a generated code that hard code the sequence of the Objects "Getters" and using the Encoder interface to encode them, e.g. if we user the example above, an EVP Serializer will look as follows:
  public class EVPSerializer {
    public void encode(Object obj,EncodeDataContainer edc){
       EVP element = (EVP)obj;
       edc.getEncoder().encodeString(element.getName(),edc);
       edc.getEncoder().encodeInt32(element.getEmployeeNumber(),edc);
       .
       .
       edc.getEncoder().encodeAndList(element.getSVPList(),edc);
    }
    public Object decode(EncodeDataContainer edc){
       EVP element = new EVP();
       element.setName(edc.getEncoder().decodeString(edc));
       element.setEmployeeNumber(edc.getEncoder().decodeInt32(edc));
       .
       .
       element.setSVPList(edc.getEncoder().decodeAndList(edc));
    }
  }

Note that the "decodeAndList" method is specifying that sub EncodeDataContainer instances will be created for each sub element that is defined as a "child" in the Tree Mode, hence Data Sand will know which objects to encode to the same "vTable" and which belong in their own "vTable". In other words, if we write a mysql database Encoder, the encodeAndList method indicates for Data Sand that the following data belongs to a different table than the current one.

[[File:DataSand-Serialization.png|thumbnail|Serialization]]
Data Sand will come with 3 default serialization methods, byte array, JSON & XML. The "will" is because i am still working on JSON & XML...:o) Adding new serialization targets is really easy and you just need to extends two classes to add a new serialization method, the Encoder & the EncodeDataContainer.

### TypeDescriptor & Serializer
A TypeDescriptor is a class that analyze an objects and defines a Serializer for that object according to rules. By default, the rules are fitted for POJOs but you can alter them and add more depending on your model and wishes. You can take a look @ the mdsal project inside Data Sand to see how to define your own rules (or observers) for your model.

A Serializer is a simple class that hard code the sequence of encoding and decoding the Object attributes, you don't have to write a Serializer as the TypeDescriptor generates and compile the serializer for your Object, either on run-time or pre project build, for your preference.

### Encoder
An Encoder contains a set of methods that each one converts a java type to the destination persistence format, for example the ByteEncoder overrides all those methods and one of them is the method "encodeInt32" to encode an "int" into a 4 byte array. By creating your own encoder, you override all the abstract methods and eventually have a class that "knows" how to convert from the POJO/Object type to persistence type and back. Objects do not need to be converted as they already have a Serializer (from the last section), hence you just need to override the basic types like int,String,List & etc.

### EncodeDataContainer
an EncodeDataContainer contains the encoded data from the Encoder, in the case of ByteEncodeDataContainer, it contains an array of bytes. In other word, the EncodeDataContainer should contain any kind of container that holds the persisted data, for example if i would have built an Encoder & EncodeDataContainer for an Oracle Database, i probably put a "PreparedStatement" and the containing container in the EncodeDataContainer.

For examples of Encoder & EncodeDataContainer, please refer to the code of ByteEncoder & ByteArrayEncodeDataContainer, JSONEncoder & JSONEncodeDataContainer, XMLEncoder & XMLEncodeDataContainer.

### ByteEncoder
I must specify that the byte encoding isn't based on the java serialization, the idea behind it is more like the Google Protocol Buffers, however without the need to define a Message for each type and compile it. E.g. the Byte encoding is encoding the Object to its minimal size (or @ least as far as i could think of...) by only encoding the data and not the types, the type codes & type definition is shared via the Clustering framework of the TypeDescriptor, which i will elaborate later on.

## Object Store
By default, Data Sand comes with proprietary File objects store that stores the data in its byte array encoding form that put to usage the Serialization section above. The unique feature of this object store is that it has a similar implementation of the XSQL in OpenDayLight (in a nutshell an SQL translator) to enable SQL queries, JDBC & Object retrieval via SQL queries. In case of a requirement to use a different persistent layer, you just need to implement two classes to introduce an new persistency layer based on the two classes you implemented in the Serialization section.

### ObjectDataStore
When you create your own persistence data store implementation, you need to extends the ObjectDataStore. In a nutshell, you just need to implement about 6 methods, only if your store needs them (like commit(), close(), init() & etc) and a constructor that indicates the type of Serializer your ObjectStore uses. Please refer to the SQLite implementation of the ObjectDataStore to see an example for a code...

### DataPersister
The Data Persister job  is to persist the encoded data to the target persistency layer, for example the ByteArrayDataPersister is taking the encoded data from the ByteArrayEncodeDataContainer and persistin g it into files. In a well establish persistency store, the Data Persister will probably not have any job, for example in an SQLIte it will probably just execute the PreparedStatement in the SQLiteEncodeDataContainer on the database connection...

# Code Examples
First we instantiate an ObjectStore, in the example i am instantiating a ByteArrayObjectDataStore. When you instantiate it, you need to specify where the data & the TypeDescriptors will be kept:
  //Instantiate a ByteArrayDataStore
  ByteArrayObjectDataStore myDataStore = new ByteArrayObjectDataStore("myExampleDataStore");
By default, Data Sand defines a set of rules for handling plain simple POJOs. Data Sand also contains a set of Rules for handling MD-SAL generated POJOs so in my example, i will use those rules so the next step is to add those rules to the ObjectStore:
  //Instantiate a ByteArrayDataStore
  ByteArrayObjectDataStore myDataStore = new ByteArrayObjectDataStore("myExampleDataStore");
  
  //The set of rules is kept in the TypeDescriptorsContainer
  //First we need to clean how POJO identify a child attribute and add MD-SAL rule for that
  myDataStore.getTypeDescriptorsContainer().clearChildAttributeObservers();
  
  //Add the MDSalObjectChildRule as the rule to identify MD-SAL child in the model
  myDataStore.getTypeDescriptorsContainer().addChildAttributeObserver(new MDSalObjectChildRule());
Let's take a quick look in the MDSalObjectChildRule to understand how the Data Sand identify a MD-SAL child attribute. As you can see below, a child attribute is identified if the return type of the POJO object is of type DataObject. It is important to state that the method "getReturnType" of the AttributeDescriptor is returning the "Component type" in case of an Array or Collection so if the method is defined as follows: "public List<MyType> ..." the AttributeDescriptor "getReturnType" will return "MyType".

  package org.opendaylight.persisted.mdsal;
  
  import org.opendaylight.datasand.codec.AttributeDescriptor;
  import org.opendaylight.datasand.codec.TypeDescriptor;
  import org.opendaylight.datasand.codec.observers.IChildAttributeObserver;
  import org.opendaylight.yangtools.yang.binding.DataObject;
  /**
   * @author - Sharon Aicler (saichler@cisco.com)
   */
  public class MDSalObjectChildRule implements IChildAttributeObserver{  
      @Override
      public boolean isChildAttribute(AttributeDescriptor ad) {
          return DataObject.class.isAssignableFrom(ad.getReturnType());
      }
      @Override
      public boolean isChildAttribute(TypeDescriptor td) {
          return DataObject.class.isAssignableFrom(td.getTypeClass());
      }
      @Override
      public boolean supportAugmentation(AttributeDescriptor ad) {
          return DataObject.class.isAssignableFrom(ad.getReturnType());
      }
      @Override
      public boolean supportAugmentation(TypeDescriptor td) {
          return DataObject.class.isAssignableFrom(td.getTypeClass());
      }
  }
Let's continue by adding the rest of the MD-SAL rules
  //Instantiate a ByteArrayDataStore
  ByteArrayObjectDataStore myDataStore = new ByteArrayObjectDataStore("myExampleDataStore");
  
  //The set of rules is kept in the TypeDescriptorsContainer
  //First we need to clean how POJO identify a child attribute and add MD-SAL rule for that
  myDataStore.getTypeDescriptorsContainer().clearChildAttributeObservers();
  
  //Add the MDSalObjectChildRule as the rule to identify MD-SAL child in the model
  myDataStore.getTypeDescriptorsContainer().addChildAttributeObserver(new MDSalObjectChildRule());
  
  //Add a type identifier rule. This rule should identify all those POJO types that are part of the Object but are not considered
  //as a child attribute, for example in YANG we can define a type and then use it as an attribute inside 
  //our Object. Those types are plain POJO types and are part of the Object and not a child attribute, hence
  //we add a rule to identify them as Data Sand needs to build them a Serializer.
  myDataStore.getTypeDescriptorsContainer().addTypeAttributeObserver(new MDSALObjectTypeRule());
  
  //By default, Data Sand will extract a class from an object by using the "getClass()" method. This will not work in cases like Dynamic Proxy implementations or in the
  //case of MD-SAL where we do not want the implementing class but the interface that it implements. So for those cases, we add a ClassExtractor rule, which in MD-SAL
  //just invoke the "getImplementedInterface" method in case the object is a DataObject.
  myDataStore.getTypeDescriptorsContainer().setClassExtractor(new MDSALClassExtractor());
  
  //By default, Data Sand ignores "get" methods like "getClass" & "getInstance". In some models we want to ignore further methods like in MD-SAL we wish to ignore 
  //the method "getImplementedInterface".
  myDataStore.getTypeDescriptorsContainer().addMethodFilterObserver(new MDSALMethodFilter());
  
  //In MD-SAL you can Augment the model so the Augmentation rule is dedicated to MD-SAL and build to handle augmentation
  myDataStore.getTypeDescriptorsContainer().setAugmentationObserver(new MDSalAugmentationObserver());
We are now ready to serialize out MD-SAL generated POJO object...
  //We write the MD-SAL myMDSalPojoObject and specify that it does not have a parent reference with "-1". Data sand keeps reference between a parent & child objects
  //by indexing them and referring to the parent index from the child.
  myDataStore.write(myMDSalPOJOObject,-1);
When a new Object is introduced for the first time EVER to Data Sand, Data Sand will generate a Serializer class and compile it @ runtime for each type & sub type it identifies in the Object. Later on, you can add those serializers to your project code base.

After adding a few of our objects to the ObjectDataStore, we are ready to write some queries:
  //Retrieve all the "Records" for a MyMDSalPojoObject. The result is a Java ResultSet and each element in the Record is a String so there are no dependencies when you use
  //JDBC from a remote machine.
  ResultSet rs = myDataStore.executeSql("Select * from MyMDSalPojoObject;");
  //If you do want to retrieve the plain objects, replace the "*" with "Objects" and you will get the plain Objects n the ResultSet item #1
  ResultSet rs = myDataStore.executeSql("Select Objects from MyMDSalPojoObject;");
=== JDBC Example ===
Let's define the following set of POJOs objects (forgive me for the one lines "getters & setters"):

  public class PojoObject {
      private int testIndex;
      private String testString;
      private boolean testBoolean;
      private long testLong;
      private short testShort;
      private SubPojoObject subPojo = null;
      private List<SubPojoList> list = new ArrayList<>();
      public PojoObject(){}
      public String getTestString() {return testString;}
      public void setTestString(String testString) {this.testString = testString;}
      public boolean isTestBoolean() {return testBoolean;}
      public void setTestBoolean(boolean testBoolean) {this.testBoolean = testBoolean;}
      public long getTestLong() {return testLong;}
      public void setTestLong(long testLong) {this.testLong = testLong;}
      public short getTestShort() {return testShort;}
      public void setTestShort(short testShort) {this.testShort = testShort;}
      public int getTestIndex() {return testIndex;}
      public void setTestIndex(int testIndex) {this.testIndex = testIndex;}
      public SubPojoObject getSubPojo() {return subPojo;}
      public void setSubPojo(SubPojoObject subPojo) {this.subPojo = subPojo;}
      public void setList(List<SubPojoList> lst){this.list=lst;}
      public List<SubPojoList> getList(){return this.list;}
  }

  public class SubPojoObject {
      private int number = -1;
      public void setNumber(int n){this.number = n;}
      public int getNumber(){return this.number;}
  }

  public class SubPojoList {
      private String name = null;
      public void setName(String n){this.name=n;}
      public String getName(){return this.name;}
  }

And define a static method to instantiate those three POJOs in the following way:
    public static PojoObject buildPojo(int pojoIndex){
        PojoObject obj = new PojoObject();
        obj.setTestIndex(pojoIndex);
        obj.setTestString("Name-"+pojoIndex);
        obj.setTestBoolean(true);
        obj.setTestLong(12345678L);
        obj.setTestShort((short)44.44);
        SubPojoObject sp = new SubPojoObject();
        obj.setSubPojo(sp);
        SubPojoList l1 = new SubPojoList();
        l1.setName("Item #"+pojoIndex+":1");
        SubPojoList l2 = new SubPojoList();
        l2.setName("Item #"+pojoIndex+":2");
        List<SubPojoList> list = new ArrayList<>();
        list.add(l1);
        list.add(l2);
        obj.setList(list);
        return obj;
    }

Now lets write a 5 nodes DataStore that each node stores 10K of those pojos:
  public static void main(String args[]){    
        //We use the ByteArrayObjectDataStore in this case.
        ByteArrayObjectDataStore stores[] = new ByteArrayObjectDataStore[5];
        //The number of POJOs we are going to push to each Node.
        int recCountPerStore = 10000;
        for(int j=0;j<stores.length;j++){
            //Create a store node with it's dedicated store directory
            // Remember, the first node being created is also the one that serves as the jvSwitch for all others
            stores[j] = new ByteArrayObjectDataStore("POJODB-"+j,true);
            //Insert 10K pojos into each store.
            for(int i=0;i<recCountPerStore;i++){
                //To make sure we could differentiate between the pojos, just set its index to j*recCountPerStore+i
                Object pojo = buildPojo(j*recCountPerStore+i);
                //store the pojo in the store
                stores[j].write(pojo, -1);
            }
            //commit the buffer to make sure the data is fully persisted
            stores[j].commit();
        }

Now let's query all the data via JDBC:
        //Well i was lazy...this can also be done via the traditional way of "Class.forname"
        //But you can also instantiate the driver without the driver manager
        DataSandJDBCDriver driver = new DataSandJDBCDriver();
        //Prepare instances of the regular JDBC inteface
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try{
            //Connect the driver to the Node network
            //the driver is also instantiating a Node, however this node is a "Unicast Node" that does not receive the regular ARP broadcasts
            //Of the virtual JVM network
            conn = driver.connect("127.0.0.1", null);
            st = conn.createStatement();
            //Prepare the select statement for query some of the data
            String sql = "Select TestString,TestBoolean,TestLong,TestShort,TestIndex from PojoObject;";
            //Execute The query on the Node Network
            rs = st.executeQuery(sql);
            //The query will immediately will reply with the MetaData so if you would like to place headers
            //or know which column is at which position, you can do that via the usual JDBC MetaData object. 
            rs.getMetaData();
            //Will just iterate over the records and count them
            int count = 0;
            //Please note that we have 5 nodes collecting the data records in a parallel method while we have only one consumer
            //that iterates over them, hence the throttling mechanism will kick in...
            while(rs.next()){
                count++;
            }
            System.out.println("Finish");
            //Compare the records count to the expected count
            Assert.assertEquals(recCountPerStore*5,count);
        }catch(Exception err){
            err.printStackTrace();
        }
        //dispose of resources
        if(rs!=null) try{rs.close();}catch(Exception err){err.printStackTrace();}
        if(st!=null) try{st.close();}catch(Exception err){err.printStackTrace();}
        if(conn!=null) try{conn.close();}catch(Exception err){err.printStackTrace();}
        //Shutdown the Node network and delete the database directories
        for(int i=0;i<stores.length;i++){
            stores[i].close();
            stores[i].deleteDatabase();
        }
  }


