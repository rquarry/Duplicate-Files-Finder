package duplicateFilesFinder;

import java.util.Comparator;

public class filePrinterComparator implements Comparator<String> {
    @Override // Netbeans likes this
    public int compare( String f1, String f2 ) {
        
        return f1.substring(0, 31).compareTo(f2.substring(0, 31));
        }
}
