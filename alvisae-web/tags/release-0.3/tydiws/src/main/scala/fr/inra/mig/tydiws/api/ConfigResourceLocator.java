/*
 *
 *      This software is a result of Quaero project and its use must respect the rules of the Quaero Project Consortium Agreement.
 *
 *      Copyright Institut National de la Recherche Agronomique, 2010-2012.
 *
 */
package fr.inra.mig.tydiws.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Service used to locate a configuration file associated with a specific instance of the WebServices
 * 
 * JNDI Custom resource Configuration
 * ----------------------------------
 * JNDI Name: AlvisAE_WS_Config
 * Resource Type: java.util.Properties
 * Factory Class: org.glassfish.resources.custom.factory.PropertiesFactory
 * 
 * for each deployed WS, a property containing the configuration filepath is added to the custom resource.
 * The property name is base on the WS contextRoot, (slash characters replaced by dots)
 * e.g. for WS deployed at : /alvisae/demo  => the property name will be : alvisae.demo.filepath


 * @author fpapazian
 */
public class ConfigResourceLocator {

    //Name of the JNDI custome resource
    private final static String JNDIName = "AlvisAE_WS_Config";
    private final static String PropSuffix = "filepath";
    private static boolean resourceRetrieved = false;
    private static Properties resProps = null;

    /**
     * 
     * @param contextRoot the context root of the application
     * @return Properties containing the values of the configuration file associated to the specified contextRoot
     */
    public static Properties getConfigFile(String contextRoot) {
        if (!resourceRetrieved) {
            try {
                Context initCtx = new InitialContext();
                Context envCtx = (Context) initCtx.lookup("");
                Object o = envCtx.lookup(JNDIName);
                if (o instanceof Properties) {
                    resProps = (Properties) o;
                }
            } catch (NamingException ex) {
            } finally {
                resourceRetrieved = true;
            }
        }
        if (resProps == null) {
            System.err.println("ConfigResourceLocator: properties '" + JNDIName + "' not found in JNDI!");
            return null;
        } else {
            String propName = contextRoot.replaceFirst("/", "").replaceAll("/", ".") + "." + PropSuffix;
            String filename = resProps.getProperty(propName);
            if (filename == null) {
                System.err.println("ConfigResourceLocator: '" + propName + "' key absent from '" + JNDIName + "'");
                return null;
            } else {
                Properties result = null;
                File f = new File(filename);
                if (f.exists() && f.canRead()) {
                    Properties temp = new Properties();
                    FileInputStream fis;
                    try {
                        fis = new FileInputStream(f);
                        try {
                            temp.load(fis);
                            System.out.println("ConfigResourceLocator: loading values from file : " + filename + "  :");
                            temp.list(System.out);
                            result = temp;
                        } catch (IOException ex) {
                            System.err.println("ConfigResourceLocator: Error while reading file : " + filename);
                            ex.printStackTrace(System.err);
                        } finally {
                            fis.close();
                        }
                    } catch (FileNotFoundException ex) {
                        System.err.println("ConfigResourceLocator: Error while opening file : " + filename);
                        ex.printStackTrace(System.err);
                    } catch (IOException ex) {
                        System.err.println("ConfigResourceLocator: Error while reading file : " + filename);
                        ex.printStackTrace(System.err);
                    }
                } else {
                    System.err.println("ConfigResourceLocator: Can not read from file : " + filename);
                    return null;
                }
                return result;
            }
        }
    }
}
