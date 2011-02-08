Development environment
=======================

To configure your javabin ivy repository settings, add the following to the <code>~/.ivy2/javabin.properties</code> file:
<pre>
host=smia.java.no
user=[your ldap username]
password=[your ldap password]
</pre>

Add something like the following to <code>~/.cms/config.properties</code>:
<pre>
serviceUrl=http://wp.java.no/?atompub=service
workspace=javazone11 Workspace
postsCollection=javazone11 Posts
pagesCollection=javazone11 Pages
</pre>

Continue with your sbt goodness...
