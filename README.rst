Finer-Grained Maven Profile Activation Extension
================================================

.. image:: https://img.shields.io/github/license/OddSource/finer-profile-activation.svg
   :target: https://github.com/OddSource/finer-profile-activation/blob/master/LICENSE.txt
.. image:: https://img.shields.io/maven-central/v/io.oddsource.java.maven/finer-profile-activation.svg
   :target: https://search.maven.org/search?q=g:io.oddsource.java%20AND%20a:oss-parent&core=gav
.. image:: https://img.shields.io/github/release/OddSource/finer-profile-activation/all.svg
   :target: https://github.com/OddSource/finer-profile-activation/releases

.. image:: https://github.com/OddSource/finer-profile-activation/actions/workflows/maven.yml/badge.svg
   :target: https://github.com/OddSource/finer-profile-activation/actions/workflows/maven.yml
.. image:: https://app.codacy.com/project/badge/Grade/4305c9ef294349288688e7b8975e9188
   :target: https://app.codacy.com/gh/OddSource/finer-profile-activation/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade
.. image:: https://app.codacy.com/project/badge/Coverage/4305c9ef294349288688e7b8975e9188
   :target: https://app.codacy.com/gh/OddSource/finer-profile-activation/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage

Automatic Maven profile activation is very limited. The :code:`<os>`, :code:`<file>`, and :code:`<property>` options
are exact matches (you can't partial match or use regular expressions), and there's often no way to distinguish
between very different operating systems. For example, using these tags, you cannot differentiate between RedHat-like
systems and Debian/Ubuntu-like systems.

This Maven lifecycle extension (not a plugin—an extension) provides much finer-grained control over
automatic profile activation, allowing you to check whether system properties, file contents, and/or command output
contains certain values or matches certain regular expressions. Like the built-in profile activators, multiple finer
activators are always AND-ed together. Additionally, you can AND built-in and finer activators together.

.. contents:: Contents
    :depth: 3

Installation
************

To "install" this extension, you must create a directory named :code:`.mvn` at the root of your project (at the same
level as your :code:`pom.xml`) and place a file named :code:`extensions.xml` with in. For example::

    .mvn/
        extensions.xml
    pom.xml
    src/
    target/
    ...

The contents of this file should be as follows (you may also/already have other extensions in this file)::

    <?xml version="1.0" encoding="UTF-8"?>
    <extensions xmlns="http://maven.apache.org/EXTENSIONS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://maven.apache.org/EXTENSIONS/1.0.0 http://maven.apache.org/xsd/core-extensions-1.0.0.xsd">
        <extension>
            <groupId>io.oddsource.java.maven</groupId>
            <artifactId>finer-profile-activation</artifactId>
            <version>[insert version here]</version>
        </extension>
    </extensions>

The version is required and cannot be parameterized. It's important to install the extension in this manner, because if
you install it directly within your :code:`pom.xml`, it will not be able to intercept profile activation.

Usage
*****

By necessity, this extension re-uses the :code:`<property>` activator with magical prefixes to trigger the finer
activators.

Property Contains
-----------------

To test that the :code:`os.name` property's contents, converted to lowercase, contains the given value::

    <activation>
        ...
        <property>
            <name>[FINER.ACTIVATOR.CONTAINS]os.name</name>
            <value>mac</value>
        </property>
        ...
    </activation>

The given property must be a property available early in the Maven lifecycle (in other words, properties specified
in the :code:`pom.xml` file will not be matched).

Property Matches Regular Expression
-----------------------------------

To test that the :code:`os.version` property's contents match the given regular expression (case sensitive)::

    <activation>
        ...
        <property>
            <name>[FINER.ACTIVATOR.REGEX]os.version</name>
            <value>^.*\.el[0-9]{1,2}(_[0-9]+)?\..*$</value><!-- enterprise linux -->
        </property>
        ...
    </activation>

File Contents Contains
----------------------

To test that the specified file exists, is accessible, and its contents contain the given value (case sensitive)::

    <activation>
        ...
        <property>
            <name>[FINER.ACTIVATOR.FILE.CONTAINS]/etc/os_release</name>
            <value>ID_LIKE=debian</value>
        </property>
        ...
    </activation>

By default, file contents are decoded in UTF-8. If this does not work, you can specify a different encoding,
using any one of the character set names `that Java supports`_::

            <name>[FINER.ACTIVATOR.FILE.CONTAINS][ISO-8859-1]/etc/os_release</name>

File Contents Matches Regular Expression
----------------------------------------

To test that the specified file exists, is accessible, and its contents contain the given regular expression
(case sensitive)::

    <activation>
        ...
        <property>
            <name>[FINER.ACTIVATOR.FILE.REGEX]/etc/os_release</name>
            <value>ID_LIKE="?(debian|fedora)"?</value>
        </property>
        ...
    </activation>

Command Execution Output Contains
---------------------------------

To test that the specified command exists, is accessible, is executable, and executes with any exit code, and its
content contains the specified value (case sensitive)::

    <activation>
        ...
        <property>
            <name>[FINER.ACTIVATOR.SUBPROCESS.CONTENT]uname -a</name>
            <value>Linux</value>
        </property>
        ...
    </activation>

Arguments are separated by spaces and you can quote arguments that contain spaces, just like on the command line::

            <name>[FINER.ACTIVATOR.SUBPROCESS.CONTENT]foo "bar baz" 'this is "one" argument'</name>

By default, process output is decoded in UTF-8. If this does not work, you can specify a different encoding,
using any one of the character set names `that Java supports`_::

            <name>[FINER.ACTIVATOR.SUBPROCESS.CONTENT][ISO-8859-1]foo "bar baz" 'this is "one" argument'</name>

Command Execution Matches Regular Expression
--------------------------------------------

To test that the specified command exists, is accessible, is executable, and executes with any exit code, and its
content matches the specified regular expression (case sensitive)::

    <activation>
        ...
        <property>
            <name>[FINER.ACTIVATOR.SUBPROCESS.REGEX]uname -a</name>
            <value>^.*\.el[0-9]{1,2}(_[0-9]+)?\..*$</value><!-- enterprise linux -->
        </property>
        ...
    </activation>

Command Execution Results in Exit Code
--------------------------------------

To test that the specified command exists, is accessible, is executable, and executes with the exit code specified
in the property value::

    <activation>
        ...
        <property>
            <name>[FINER.ACTIVATOR.SUBPROCESS.REGEX]some_command --with-argument --and-another "foo bar baz"</name>
            <value>0</value>
        </property>
        ...
    </activation>

Debugging
*********

If you're having a hard time getting your finer activation rules to work, you can execute this command to view
exhaustive debug output of this extension's evaluation of your rules::

    $ mvn --debug help:active-profiles

You should see output like this that indicates how the extension is working::

    [DEBUG] Property name '[FINER.ACTIVATOR.SUBPROCESS.REGEX]uname -a' matches finer activator pattern.
    [DEBUG] Finer activator 'FINER.ACTIVATOR.FILE.CONTAINS' applicable to property name '[FINER.ACTIVATOR.SUBPROCESS.REGEX]uname -a' = false
    [DEBUG] Finer activator 'FINER.ACTIVATOR.FILE.CONTAINS' INACTIVE
    [DEBUG] Finer activator 'FINER.ACTIVATOR.FILE.REGEX' applicable to property name '[FINER.ACTIVATOR.SUBPROCESS.REGEX]uname -a' = false
    [DEBUG] Finer activator 'FINER.ACTIVATOR.FILE.REGEX' INACTIVE
    [DEBUG] Finer activator 'FINER.ACTIVATOR.CONTAINS' applicable to property name '[FINER.ACTIVATOR.SUBPROCESS.REGEX]uname -a' = false
    [DEBUG] Finer activator 'FINER.ACTIVATOR.CONTAINS' INACTIVE
    [DEBUG] Finer activator 'FINER.ACTIVATOR.REGEX' applicable to property name '[FINER.ACTIVATOR.SUBPROCESS.REGEX]uname -a' = false
    [DEBUG] Finer activator 'FINER.ACTIVATOR.REGEX' INACTIVE
    [DEBUG] Finer activator 'FINER.ACTIVATOR.SUBPROCESS.EXIT' applicable to property name '[FINER.ACTIVATOR.SUBPROCESS.REGEX]uname -a' = false
    [DEBUG] Finer activator 'FINER.ACTIVATOR.SUBPROCESS.EXIT' INACTIVE
    [DEBUG] Finer activator 'FINER.ACTIVATOR.SUBPROCESS.CONTENT' applicable to property name '[FINER.ACTIVATOR.SUBPROCESS.REGEX]uname -a' = false
    [DEBUG] Finer activator 'FINER.ACTIVATOR.SUBPROCESS.CONTENT' INACTIVE
    [DEBUG] Finer activator 'FINER.ACTIVATOR.SUBPROCESS.REGEX' applicable to property name '[FINER.ACTIVATOR.SUBPROCESS.REGEX]uname -a' = true
    [DEBUG] Finer activator 'FINER.ACTIVATOR.SUBPROCESS.REGEX' INACTIVE

This should enable you to figure out what's going on.

.. _that Java supports: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/charset/Charset.html#forName(java.lang.String)
