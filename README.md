
# Anypoint Template: Salesforce to Salesforce Lead Broadcast

# License Agreement
This template is subject to the conditions of the 
<a href="https://s3.amazonaws.com/templates-examples/AnypointTemplateLicense.pdf">MuleSoft License Agreement</a>.
Review the terms of the license before downloading and using this template. You can use this template for free 
with the Mule Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case
As a Salesforce admin I want to synchronize Leads between two Salesforce orgs.

This Anypoint Template should serve as a foundation for setting an online sync of Leads from one SalesForce instance to another. Every time there is new Lead or a change in an already existing one, the integration will poll for changes in SalesForce source instance and it will be responsible for updating the Lead on the target org.

Requirements have been set not only to be used as examples, but also to establish a starting point to adapt your integration to your requirements.

As implemented, this Anypoint Template leverages the [Batch Module](http://www.mulesoft.org/documentation/display/current/Batch+Processing).
The batch job is divided in Process and On Complete stages.
During the Input stage the Anypoint Template will go to the SalesForce Org A and query all the existing Leads that match the filter criteria.
During the Process stage, each SalesForce Lead will be filtered depending on, if it has an existing matching Lead in the SalesForce Org B.
The last step of the Process stage will group the Leads and create/update them in SalesForce Org B.
Finally during the On Complete stage the Anypoint Template will log output statistics data into the console.

# Considerations

To make this Anypoint Template run, there are certain preconditions that must be considered. All of them deal with the preparations in both source and destination systems, that must be made in order for all to run smoothly. **Failling to do so could lead to unexpected behavior of the template.**



## Salesforce Considerations

Here's what you need to know about Salesforce to get this template to work.

### FAQ

- Where can I check that the field configuration for my Salesforce instance is the right one? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US">Salesforce: Checking Field Accessibility for a Particular Field</a>
- Can I modify the Field Access Settings? How? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US">Salesforce: Modifying Field Access Settings</a>

### As a Data Source

If the user who configured the template for the source system does not have at least *read only* permissions for the fields that are fetched, then an *InvalidFieldFault* API fault displays.

```
java.lang.RuntimeException: [InvalidFieldFault [ApiQueryFault [ApiFault  exceptionCode='INVALID_FIELD'
exceptionMessage='
Account.Phone, Account.Rating, Account.RecordTypeId, Account.ShippingCity
^
ERROR at Row:1:Column:486
No such column 'RecordTypeId' on entity 'Account'. If you are attempting to use a custom field, be sure to append the '__c' after the custom field name. Reference your WSDL or the describe call for the appropriate names.'
]
row='1'
column='486'
]
]
```

### As a Data Destination

There are no considerations with using Salesforce as a data destination.









# Run it!
Simple steps to get Salesforce to Salesforce Lead Broadcast running.
See below.

## Running On Premises
Complete all properties in one of the property files, for example in [mule.prod.properties] (../blob/master/src/main/resources/mule.prod.properties) and run your app with the corresponding environment variable to use it. To follow the example, this will be `mule.env=prod`.

Once your app is all set and started, there is no need to do anything else. The application will poll SalesForce to know if there are any newly created or updated objects and synchronice them.


### Where to Download Anypoint Studio and the Mule Runtime
If you are a newcomer to Mule, here is where to get the tools.

+ [Download Anypoint Studio](https://www.mulesoft.com/platform/studio)
+ [Download Mule runtime](https://www.mulesoft.com/lp/dl/mule-esb-enterprise)


### Importing a Template into Studio
In Studio, click the Exchange X icon in the upper left of the taskbar, log in with your
Anypoint Platform credentials, search for the template, and click **Open**.


### Running on Studio
After you import your template into Anypoint Studio, follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources.
+ Complete all the properties required as per the examples in the "Properties to Configure" section.
+ Right click the template project folder.
+ Hover your mouse over `Run as`
+ Click `Mule Application (configure)`
+ Inside the dialog, select Environment and set the variable `mule.env` to the value `dev`
+ Click `Run`


### Running on Mule Standalone
Complete all properties in one of the property files, for example in mule.prod.properties and run your app with the corresponding environment variable. To follow the example, this is `mule.env=prod`. 


## Running on CloudHub
While creating your application on CloudHub (or you can do it later as a next step), go to Runtime Manager > Manage Application > Properties to set the environment variables listed in "Properties to Configure" as well as the **mule.env**.
Once your app is all set and started, there is no need to do anything else. Every time a Lead is created or modified, it will be automatically synchronised to SalesForce Org B as long as it has an Email.

### Deploying your Anypoint Template on CloudHub
Studio provides an easy way to deploy your template directly to CloudHub, for the specific steps to do so check this


## Properties to Configure
To use this template, configure properties (credentials, configurations, etc.) in the properties file or in CloudHub from Runtime Manager > Manage Application > Properties. The sections that follow list example values.
### Application Configuration
**Application configuration**

+ scheduler.frequency `60000`
+ scheduler.startDelay `0`
+ watermark.defaultExpression `YESTERDAY`
+ page.size `200`
+ owner.sync.policy `syncOwner`

**Note:** the property **owner.sync.policy** can take any of the two following values: 

+ **empty_value**: if the propety has no value assigned to it then application will do nothing in what respect to the lead and it'll just move the lead over.
+ **syncOwner**: it will try to create the lead's owner if there is no occurence in the Salesforce instance B.


#### SalesForce Connector configuration for company A
+ sfdc.a.username `bob.dylan@orga`
+ sfdc.a.password `DylanPassword123`
+ sfdc.a.securityToken `avsfwCUl7apQs56Xq2AKi3X`

#### SalesForce Connector configuration for company B
+ sfdc.b.username `joan.baez@orgb`
+ sfdc.b.password `JoanBaez456`
+ sfdc.b.securityToken `ces56arl7apQs56XTddf34X`
 
+ sfdc.b.user.profile.id `00a21000001UzDr`

# API Calls
Salesforce imposes limits on the number of API Calls that can be made. Therefore calculating this amount may be an important factor to consider. The Anypoint template calls to the API can be calculated using the formula:

***1 + X + X / 200***

Being ***X*** the number of Leads to be synchronized on each run. 

The division by ***200*** is because, by default, Leads are gathered in groups of 200 for each Upsert API Call in the commit step. Also consider that this calls are executed repeatedly every polling cycle.	

For instance if 10 records are fetched from origin instance, then 12 api calls will be made (1 + 10 + 1).


# Customize It!
This brief guide intends to give a high level idea of how this template is built and how you can change it according to your needs.
As Mule applications are based on XML files, this page describes the XML files used with this template.

More files are available such as test classes and Mule application files, but to keep it simple, we focus on these XML files:

* config.xml
* businessLogic.xml
* endpoints.xml
* errorHandling.xml


## config.xml
Configuration for connectors and configuration properties are set in this file. Even change the configuration here, all parameters that can be modified are in properties file, which is the recommended place to make your changes. However if you want to do core changes to the logic, you need to modify this file.

In the Studio visual editor, the properties are on the *Global Element* tab.


## businessLogic.xml
Functional aspect of the Anypoint Template is implemented on this XML, directed by a batch job that will be responsible for creations/updates. The several message processors constitute four high level actions that fully implement the logic of this Anypoint Template:

1. Job execution is invoked from triggerFlow (endpoints.xml) everytime there is new query executed asking for created/updated Leads.
2. During the Process stage, each SalesForce Lead will be filtered depending on, if it has an existing matching Lead in the SalesForce Org B.
3. The last step of the Process stage will group the Leads and create/update them in SalesForce Org B.
4. Finally during the On Complete stage the Anypoint Template will logoutput statistics data into the console.



## endpoints.xml
This file is conformed by two Flows.

The first one we'll call it **trigger** flow. This one contains the Scheduler endpoint that will periodically trigger **watermark** flow and then executing the batch job process.

The second one we'll call it **watermark** flow. This one contains watermarking logic that will be querying Salasforce for updated/created Leads that meet the defined criteria in the query since the last polling. The last invocation timestamp is stored by using Objectstore Component and updated after each Salasforce query.



## errorHandling.xml
This is the right place to handle how your integration reacts depending on the different exceptions. 
This file provides error handling that is referenced by the main flow in the business logic.




