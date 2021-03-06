**Liferay Connector**

This is a **work-in-progress** REST connector for [Evolveum Midpoint](https://wiki.evolveum.com/). There already exist a [SOAP](https://github.com/Evolveum/connector-liferay) based connector. This project is for my learning and because I prefer to use REST and JSON. :)

**Things that work:**

 - Add/Update/Delete User
 - Add/Update/Delete Roles
 - Add/Update/Delete User Groups
 - Add/Update/Delete Websites
 - Add/Update/Delete Organizations
 - Role associations
 - User Group associations
 - Website associations
 - Organization associations
 - Paging added for user account

**Install:**
 - clone this repo && mvn package



Association of roles/user groups/websites/organizations example:

 ```XML
     <inducement>
         <construction>
             <resourceRef oid="<Your OID>" relation="org:default" type="c:ResourceType">
             </resourceRef>
             <kind>entitlement</kind>
             <intent>role</intent>
         </construction>
     </inducement>

     <inducement>
         <construction>
             <resourceRef oid="<Your OID>" relation="org:default" type="c:ResourceType">
             </resourceRef>
             <kind>account</kind>
             <intent>default</intent>
             <association>
                 <c:ref>ri:role</c:ref>
                 <outbound>
                     <expression>
                         <associationFromLink xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="c:AssociationFromLinkExpressionEvaluatorType">
                             <projectionDiscriminator xsi:type="c:ShadowDiscriminatorType">
                                 <kind>entitlement</kind>
                                 <intent>role</intent>
                             </projectionDiscriminator>
                         </associationFromLink>
                     </expression>
                 </outbound>
             </association>
         </construction>
         <order>2</order>
     </inducement>
```
