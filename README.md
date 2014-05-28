# Anypoint Template: Salesforce to Salesforce Lead Broadcast

+ [Use Case](#usecase)
+ [Run it!](#runit)
    * [Running on premise](#runonopremise)
    * [Running on CloudHub](#runoncloudhub)
    * [Properties to be configured](#propertiestobeconfigured)
+ [API Calls](#apicalls)
+ [Customize It!](#customizeit)
    * [config.xml](#configxml)
    * [endpoints.xml](#endpointsxml)
    * [businessLogic.xml](#businesslogicxml)
    * [errorHandling.xml](#errorhandlingxml)
+ [Testing the Anypoint Template](#testingtheanypointtemplate)
 

# Use Case <a name="usecase"/>
As a Salesforce admin I want to syncronize Leads between two Salesfoce orgs.

This Anypoint Template should serve as a foundation for setting an online sync of Lead from one SalesForce instance to another. Everytime there is a new Lead or a change in an already existing one, the integration will poll for changes in SalesForce source instance and it will be responsible for updating the Lead on the target org.

Requirements have been set not only to be used as examples, but also to establish a starting point to adapt your integration to your requirements.

As implemented, this Anypoint Template leverage the [Batch Module](http://www.mulesoft.org/documentation/display/current/Batch+Processing).
The batch job is divided in Input, Process and On Complete stages.
During the Input stage the Anypoint Template will go to the SalesForce Org A and query all the existing Leads that match the filter criteria.
During the Process stage, each SalesForce Lead will be filtered depending on, if it has an existing matching Lead in the SalesForce Org B.
The last step of the Process stage will group the Leads and create/update them in SalesForce Org B.
Finally during the On Complete stage the Anypoint Template will logoutput statistics data into the console.

# Run it! <a name="runit"/>

In order to have the template up and running just complete the two following steps:

 1. [Configure the application properties](#propertiestobeconfigured)
 2. Run it! ([on premise](#runonopremise) or [in Cloudhub](#runoncloudhub))

## Running on premise <a name="runonopremise"/>

In this section we detail the way you have to run you Anypoint Temple on you computer.

### Running on Studio <a name="runonstudio"/>
Once you have imported you Anypoint Template into Anypoint Studio you need to follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources
+ Complete all the properties required as per the examples in the section [Properties to be configured](#propertiestobeconfigured)
+ Once that is done, right click on you Anypoint Template project folder 
+ Hover you mouse over `"Run as"`
+ Click on  `"Mule Application"`


### Running on Mule ESB stand alone <a name="runonmuleesbstandalone"/> 
Complete all properties in one of the property files, for example in [mule.prod.properties] (../blob/master/src/main/resources/mule.prod.properties) and run your app with the corresponding environment variable to use it. To follow the example, this will be `mule.env=prod`.


## Running on CloudHub <a name="runoncloudhub"/>

While [creating your application on CloudHub](http://www.mulesoft.org/documentation/display/current/Hello+World+on+CloudHub) (Or you can do it later as a next step), you need to go to Deployment > Advanced to set all environment variables detailed in **Properties to be configured** as well as the **mule.env**. 

Once your app is all set and started, there is no need to do anything else. Every time a Lead is created or modified, it will be automatically synchronised to SalesForce Org B as long as it has an Email.

### Deploying your Anypoint Template on CloudHub <a name="deployingyouranypointtemplateoncloudhub"/>
 	
Mule Studio provides you with really easy way to deploy your Anypoint Template directly to CloudHub, for the specific steps to do so please check this [link](http://www.mulesoft.org/documentation/display/current/Deploying+Mule+Applications#DeployingMuleApplications-DeploytoCloudHub)

## Running on premise <a name="runonopremise"/>
Complete all properties in one of the property files, for example in [mule.prod.properties] (../blob/master/src/main/resources/mule.prod.properties) and run your app with the corresponding environment variable to use it. To follow the example, this will be `mule.env=prod`.

Once your app is all set and started, there is no need to do anything else. The application will poll SalesForce to know if there are any newly created or updated objects and synchronice them.

## Properties to be configured (With examples) <a name="propertiestobeconfigured"/>

In order to use this Mule Anypoint Template you need to configure properties (Credentials, configurations, etc.) either in properties file or in CloudHub as Environment Variables. Detail list with examples:

### Application configuration
+ polling.frequency `60000`
+ poll.startDelayMillis `0`
+ watermark.defaultExpression `YESTERDAY`
+ account.sync.policy `syncAccount`
+ account.id.in.b `001n0000003fMWXAA2`

**Note:** the property **account.sync.policy** can take any of the three following values: 

+ **empty_value**: if the propety has no value assigned to it then application will do nothing in what respect to the account and it'll just move the Lead over.
+ **syncAccount**: it will try to create the Lead's account should this is not pressent in the Salesforce instance B.
+ **assignDummyAccount**: it will assign the Lead to an pre existing account in Salesforce instance B. For this it will use the value of  `account.id.in.b`. Finding the Id of the desired Account can be done by executing in your **Sales Force Developer Console** the following query: `SELECT Id, Name, Description FROM Account`.



#### SalesForce Connector configuration for company A
+ sfdc.a.username `bob.dylan@orga`
+ sfdc.a.password `DylanPassword123`
+ sfdc.a.securityToken `avsfwCUl7apQs56Xq2AKi3X`
+ sfdc.a.url `https://login.salesforce.com/services/Soap/u/26.0`

#### SalesForce Connector configuration for company B
+ sfdc.b.username `joan.baez@orgb`
+ sfdc.b.password `JoanBaez456`
+ sfdc.b.securityToken `ces56arl7apQs56XTddf34X`
+ sfdc.b.url `https://login.salesforce.com/services/Soap/u/26.0`


# API Calls <a name="apicalls"/>

SalesForce imposes limits on the number of API Calls that can be made. Therefore calculating this amount may be an important factor to consider. Lead Broadcast Template calls to the API can be calculated using the formula:

***1 + X + X / 200***

Being ***X*** the number of Leads to be synchronized on each run. 

The division by ***200*** is because, by default, Leads are gathered in groups of 200 for each Upsert API Call in the commit step. Also consider that this calls are executed repeatedly every polling cycle.	

For instance if 10 records are fetched from origin instance, then 12 api calls will be made (1 + 10 + 1).


# Customize It!<a name="customizeit"/>

This brief guide intends to give a high level idea of how this Anypoint Template is built and how you can change it according to your needs.
As mule applications are based on XML files, this page will be organized by describing all the XML that conform the Anypoint Template.
Of course more files will be found such as Test Classes and [Mule Application Files](http://www.mulesoft.org/documentation/display/current/Application+Format), but to keep it simple we will focus on the XMLs.

Here is a list of the main XML files you'll find in this application:

* [config.xml](#configxml)
* [inboundEndpoints.xml](#inboundendpointsxml)
* [businessLogic.xml](#businesslogicxml)
* [errorHandling.xml](#errorhandlingxml)


## config.xml<a name="configxml"/>
Configuration for Connectors and [Properties Place Holders](http://www.mulesoft.org/documentation/display/current/Configuring+Properties) are set in this file. **Even you can change the configuration here, all parameters that can be modified here are in properties file, and this is the recommended place to do it so.** Of course if you want to do core changes to the logic you will probably need to modify this file.

In the visual editor they can be found on the *Global Element* tab.


## businessLogic.xml<a name="businesslogicxml"/>
Functional aspect of the Anypoint Template is implemented on this XML, directed by a batch job that will be responsible for creations/updates. The severeal message processors constitute four high level actions that fully implement the logic of this Anypoint Template:

1. Job execution is invoked from triggerFlow (inboundEndpoints.xml) everytime there is a new query executed asking for created/updated Leads.
2. During the Process stage, each SalesForce Lead will be filtered depending on, if it has an existing matching Lead in the SalesForce Org B.
3. The last step of the Process stage will group the Leads and create/update them in SalesForce Org B.
Finally during the On Complete stage the Anypoint Template will logoutput statistics data into the console.

## endpoints.xml<a name="endpointsxml"/>
This is file is conformed by a Flow containing the Poll that will periodically query Sales Force for updated/created Leads that meet the defined criteria in the query. And then executing the batch job process with the query results.

## errorHandling.xml<a name="errorhandlingxml"/>
Contains a [Catch Exception Strategy](http://www.mulesoft.org/documentation/display/current/Catch+Exception+Strategy) that is only Logging the exception thrown (If so). As you imagine, this is the right place to handle how your integration will react depending on the different exceptions.

# Testing the Anypoint Template <a name="testingtheanypointtemplate"/>

You will notice that the Anypoint Template has been shipped with test.
These devidi them self into two categories:

+ Unit Tests
+ Integration Tests

You can run any of them by just doing right click on the class and clicking on run as Junit test.

Do bear in mind that you'll have to tell the test classes which property file to use.
For you convinience we have added a file mule.test.properties located in "src/test/resources".
In the run configurations of the test just make sure to add the following property:

+ -Dmule.env=test
