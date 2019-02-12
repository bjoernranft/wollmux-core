package de.muenchen.allg.itd51.wollmux.core.dialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.awt.Rectangle;
import com.sun.star.awt.WindowAttribute;
import com.sun.star.awt.WindowClass;
import com.sun.star.awt.WindowDescriptor;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XFrames;
import com.sun.star.frame.XFramesSupplier;
import com.sun.star.frame.XLayoutManager;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;

import de.muenchen.allg.afid.UNO;

public class UNODialogFactory
{
  private static final Logger LOGGER = LoggerFactory
      .getLogger(UNODialogFactory.class);

  private XWindow modalBaseDialogWindow = null;
  private XLayoutManager layoutManager = null;
  private XFramesSupplier xFramesSupplier = null;
  private XWindow contXWindow = null;
  
  public XWindow createDialog(int width, int height, int backgroundColor)
  {
    Object cont = UNO.createUNOService("com.sun.star.awt.UnoControlContainer");
    XControl dialogControl = UnoRuntime.queryInterface(XControl.class, cont);

    Object unoControlContainerModelO = UNO
        .createUNOService("com.sun.star.awt.UnoControlContainerModel");
    XControlModel unoControlContainerModel = UnoRuntime
        .queryInterface(XControlModel.class, unoControlContainerModelO);
    dialogControl.setModel(unoControlContainerModel);

    contXWindow = UNO.XWindow(dialogControl);

    Object toolkit = null;
    XToolkit xToolkit = null;
    try
    {
      toolkit = UNO.xMCF.createInstanceWithContext(
          "com.sun.star.awt.Toolkit", UNO.defaultContext);
      xToolkit = UnoRuntime.queryInterface(XToolkit.class, toolkit);
    } catch (Exception e)
    {
      LOGGER.error("", e);
    }
   
    XWindow currentWindow = UNO.desktop.getCurrentFrame().getContainerWindow();
    XWindowPeer currentWindowPeer = UNO.XWindowPeer(currentWindow);
    XWindowPeer modalBaseDialog = createModalBaseDialog(xToolkit,
        currentWindowPeer, width, height);
    this.modalBaseDialogWindow = UNO.XWindow(modalBaseDialog);

    Object testFrame;
    XFrame xFrame = null;
    try
    {
      testFrame = UNO.xMCF.createInstanceWithContext(
          "com.sun.star.frame.Frame", UNO.defaultContext);
      
      xFrame = UNO.XFrame(testFrame);
      xFrame.setName("DefaultFrame");
    } catch (Exception e)
    {
      LOGGER.error("", e);
    }

    xFrame.initialize(this.modalBaseDialogWindow);
    xFramesSupplier = UNO.desktop.getCurrentFrame().getCreator();
    XFrames xFrames = xFramesSupplier.getFrames();
    xFrames.append(xFrame);
    xFrame.setCreator(xFramesSupplier);
    xFrame.activate();
    
    XPropertySet propertySet = UnoRuntime.queryInterface(XPropertySet.class, xFrame);
    
    try
    {
      Object PropertySetObject = propertySet.getPropertyValue("LayoutManager");
      layoutManager = UnoRuntime.queryInterface(XLayoutManager.class, PropertySetObject);
    } catch (UnknownPropertyException e)
    {
      LOGGER.error("", e);
    } catch (WrappedTargetException e)
    {
      LOGGER.error("", e);
    }
    
    dialogControl.createPeer(xToolkit, modalBaseDialog);
    XWindowPeer testPeer = dialogControl.getPeer();
    testPeer.setBackground(backgroundColor);

    boolean isSuccessfullySet = xFrame.setComponent(contXWindow, null);

    if (!isSuccessfullySet)
    {
      LOGGER.error(
          "UNODialogExample: createDialog: XFrame has not been set successfully.");
      return contXWindow;
    }

    return contXWindow;
  }
  
  public XWindow addXFrameToLayoutManager(String name) {
    
    if (layoutManager == null) {
      LOGGER.error("UNODialogFactory: addXFrameToLayoutManager: layoutManager is NULL.");
      return null;
    }
    
    Object testFrame;
    XFrame xFrame = null;
    XWindow containerWindow = null;
    try
    {
      testFrame = UNO.xMCF.createInstanceWithContext(
          "com.sun.star.frame.Frame", UNO.defaultContext);
      
      xFrame = UNO.XFrame(testFrame);
      XFrames frames = xFramesSupplier.getFrames();
//      xFrame.initialize(modalBaseDialogWindow);
//      xFrame.setCreator(xFramesSupplier);
      xFrame.setName(name);
      frames.append(xFrame);

      //xFrame.deactivate();
      
      Object cont = UNO.createUNOService("com.sun.star.awt.UnoControlContainer");
      XControl dialogControl = UnoRuntime.queryInterface(XControl.class, cont);

      Object unoControlContainerModelO = UNO
          .createUNOService("com.sun.star.awt.UnoControlContainerModel");
      XControlModel unoControlContainerModel = UnoRuntime
          .queryInterface(XControlModel.class, unoControlContainerModelO);
      dialogControl.setModel(unoControlContainerModel);

      containerWindow = UNO.XWindow(dialogControl);
      //xFrame.initialize(contXWindow);
      //xFrame.setCreator(xFramesSupplier);
      //xFrame.setComponent(contXWindow, null);
      //XControlContainer container = UnoRuntime.queryInterface(XControlContainer.class, contXWindow);
      //layoutManager.attachFrame(xFrame);
      //layoutManager.doLayout();
    } catch (Exception e)
    {
      LOGGER.error("", e);
    }
    
    return containerWindow;
  }
  
  public void setActiveFrame(String frameName) {
    for (int i = 0; i < getXFramesSupplier().getFrames().getCount(); i++) {
      try
      {
        XFrame xFrame = UNO.XFrame(getXFramesSupplier().getFrames().getByIndex(i));
        
        if (xFrame.getName() != null && xFrame.getName().equals(frameName)) {
          xFrame.activate();
        } else {
          XFrames frames = xFramesSupplier.getFrames();
          frames.remove(xFrame);
        }
      } catch (IndexOutOfBoundsException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (WrappedTargetException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  public XFramesSupplier getXFramesSupplier() {
    if (xFramesSupplier == null) {
      LOGGER.error("UNODialogFactory: getXFramesSupplier: xFramesSupplier is NULL");
      return null;
    }
    
    return xFramesSupplier;
  }
  
  public void showDialog() {
    if(this.modalBaseDialogWindow == null) {
      LOGGER.error("Es wurde kein exestierendes Dialog-Fenster gefunden. Ein Dialog muss zuvor erstellt werden.");
      return;
    }
    
    this.modalBaseDialogWindow.setEnable(true);
    this.modalBaseDialogWindow.setVisible(true);
  }
  
  public void closeDialog() {
    if(this.modalBaseDialogWindow == null) {
      LOGGER.error("Es wurde kein exestierendes Dialog-Fenster gefunden. Ein Dialog muss zuvor erstellt werden.");
      return;
    }
    
    this.modalBaseDialogWindow.setEnable(false);
    this.modalBaseDialogWindow.dispose();
    this.modalBaseDialogWindow = null;
  }

  private XWindowPeer createModalBaseDialog(XToolkit toolkit,
      XWindowPeer parentWindow, int width, int height)
  {
    com.sun.star.awt.Rectangle rect = new Rectangle();

    XWindow parentXWindow = UNO.XWindow(parentWindow);
    rect.X = (parentXWindow.getPosSize().Width / 2) - (width / 2);
    rect.Y = (parentXWindow.getPosSize().Height / 2) - (height / 2);
    rect.Width = width;
    rect.Height = height;

    WindowDescriptor aWindow = new WindowDescriptor();
    aWindow.Type = WindowClass.TOP;
    aWindow.WindowServiceName = "window";
    aWindow.Parent = parentWindow;
    aWindow.ParentIndex = -1;
    aWindow.Bounds = rect;

    aWindow.WindowAttributes = WindowAttribute.CLOSEABLE
        | WindowAttribute.SIZEABLE | WindowAttribute.MOVEABLE
        | WindowAttribute.BORDER;

    return toolkit.createWindow(aWindow);
  }
}
