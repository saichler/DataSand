# DataSand

## Intoduction

Today you can easily jump start a few VMs and connect them using some NFV function, however if you would like to write a software that is following the same paradigm for your service, e.g. jump start processes that host your micro service in a horizontal scale way, define communication between your micro service and handle high availability of your micro service, you will probably be writing your PaaS from scratch, spending huge amount of time even before implementing a single micro service and facing challenges of serialization, process 2 process communication, VM 2 VM communication, edge (mobile) communication, data mining of your service & etc.

In a nutshell, DataSand comes to ease the pain and allow you to develop a micro service without little consideration on what VM they are running, what process or even if they are running on a mobile device.

## The Habitat

Say you just opened a company that gives some kind of standard service, for example insurance. @ first, you are your own boss with no employees but you still need your business habitat, which is a small office cube, a phone, a computer & filer to start and serve your customers.
 
Same in DataSand, you instantiate a MicroServiceManager that instantiate a ServiceHabitat that includes model Seriaization, Networking and data store services for your MicroService so Initially you just extend the MicroService, implement  your business logic of your MicroService & add it to your MicroServiceManager.

#### Instantiate the micro service manager that will also instantiate the habitat for your micro service
MicroServiceManager msm = new MicroServiceManager();

#### Instantiate your micro service
#### First argument is your habitat ID so the MicroService can create its ID under this habitat
#### Second argument is “who is your manager”
MyMicroService mms = new MyMicroService(msm.getHabitat().getLocalHost(),msm);

## Adding More Services

Now business is doing good and you decided to hire your first employee, your first employee is doing the exact same work you are doing and share the load. You now examine your habitat (your office) and see there enough space to place the new employee in your habitat (your office) so you just share the habitat doing the same job (of course with his own desk, phone # & own filer).
Same in DataSand, you just instantiate another micro service and add it to the micro service manager so they share the same habitat.

//just instantiate another of your service
MyMicroService mms2 = new MyMicroService(msm.getHabitat().getLocalHost(),msm);

How does your micro service communicate with each other? An explanation will come later on…:o)

## Another Habitat
So you outgrown your office, need to add another one. Your service providers make sure your communication & computers are commected so both offices, although in different floors in the same building, see as one.

Same in DataSand, you just instantiate another MicroServiceManager in a different process. The Habitats will automatically form a network, making the MicroService communication transparent as they were implemented under the same Habitat.

## Another Machine
So you outgrown your building and need to open another office in a different building…

Same in DataSand, you just need to instantiate a MicroServiceManager in another VM, if the VMs are in the same subnet, the Habitats will connect automatically with no effort on your side and the communication and service will be transparent to your MicroService as if they were on the same process…:o)

# Work in progress....still writing documentation
