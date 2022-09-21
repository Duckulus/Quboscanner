package me.replydev.qubo;
import com.formdev.flatlaf.FlatDarculaLaf;
import me.replydev.mcping.net.Check;
import me.replydev.qubo.gui.MainWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public class Main {
    public static Logger logger = LogManager.getLogger("Main");

    public static void main(String[] args) throws InterruptedException {

        if(args.length == 0){
            FlatDarculaLaf.install();
            JFrame.setDefaultLookAndFeelDecorated(true);
            Info.gui = true;
            new MainWindow();
        }
        else {
            Info.gui = false;
            CLI.init(args);
        }
    }
}