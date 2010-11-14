package p2pclient;

/**
 *
 * @author Jeremy
 */
public class ComponentTester {
    public static void main (String args[]) {
        TestTrackerQueryImportExport();
        TestUtilExtractNullTerminatedString();
    }
    
    private static void TestTrackerQueryImportExport() {
        String filename = "Testfilename.ext";
        int listeningPort = 44235;
        TrackerQuery trackerQuery = new TrackerQuery(filename, listeningPort);
        byte[] messageData = trackerQuery.ExportQuery();

        System.out.println("Filename = " + trackerQuery.filename +
                ", listeningPort = " + trackerQuery.listeningPort +
                ", messageData (" + messageData.length + ")");

        TrackerQuery trackerQueryImport = new TrackerQuery();

        if (trackerQueryImport.ImportQuery(messageData)) {
            System.out.println("Import reported success: Filename = " + trackerQueryImport.filename +
                    ", listeningPort = " + trackerQueryImport.listeningPort);
        } else {
            System.out.println("Import reported failure");
        }
    }

    private static void TestUtilExtractNullTerminatedString() {
        String test2 = "Firstteststring" + '\0' + "Secondteststring" + '\0' + "Thirdteststring";
        byte[] test2bytes = test2.getBytes();
        int currentIndex = 0;
        int[] nextIndex = {0};
        String string;
        int i;

        for (i = 0; i < 3; i++) {
            if ((string = Util.ExtractNullTerminatedString(test2bytes, currentIndex, nextIndex)) != null) {
                System.out.println("Iteration " + i + ": String = " + string +
                        "; currentIndex = " + currentIndex + "; nextIndex = " + nextIndex[0]);
                currentIndex = nextIndex[0];
            } else {
                System.out.println("Iteration " + i + ": NULL");
            }
        }
    }
}

