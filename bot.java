import java.util.*;
import edu.stanford.nlp.simple.*;

//prototype chatbot designed to make deposits and withdrawals out of a virtual bank account
//powered by Stanford CoreNLP version 4.5.4 - some code constructed using chatgpt and sample code from Stanford University
//created by Jericho Jacala jjacala@bu.edu 2023
public class bot {
    public static void main(String[] args) {
        boolean satisfied = false;
        int balance = 0;
        System.out.println("Hello! My name is C-3P0 and I am an AI teller. How can I help you today? Please remember to type any numbers in number form i.e, 50. Type \"EXIT\" to sign out.");
        Scanner scan = new Scanner(System.in);
        // Create a document. No computation is done yet.
        balance = processRequest(scan, balance);
        while (!satisfied){
            System.out.println("Is there anything else I can help you with? Type \"EXIT\" to sign out.");
            balance = processRequest(scan, balance);
        }
        scan.close();
    }
    //detection system for key command words
    public static boolean containsLemma(List<String> lemmas, String str){
        for (String lemma: lemmas){
            if (lemma.equalsIgnoreCase(str)){
                return true;
            }
        }
        return false;
    }

    //detection system for numbers
    public static int detectAmount(List<Token> tokens){
        int amount;
        for (Token token : tokens) {
            // Check if the token is a number
            if ((token.ner().equalsIgnoreCase("NUMBER") || token.ner().equalsIgnoreCase("MONEY")) && !token.word().equals("$")) {
                amount = Integer.parseInt(token.word());
                System.out.println("Number detected: " + amount);
                return amount;
            }
        }
        return 0;
    }

    public static int processRequest(Scanner scan, int balance){
        String input = scan.nextLine();
        Document doc = new Document(input);
        boolean wantDeposit = false;
        boolean wantWithdrawal = false;
        boolean wantBalance = false;
        int amount = 0;
        for (Sentence sent : doc.sentences()) {
            List<String> lemmas = sent.lemmas();
            if (containsLemma(lemmas, "withdrawal") || containsLemma(lemmas, "withdraw") || containsLemma(lemmas, "take")){
                wantWithdrawal = true;
            }
            if (containsLemma(lemmas, "deposit") || containsLemma(lemmas, "put")){
                wantDeposit = true;
            }
            if (containsLemma(lemmas, "much") || containsLemma(lemmas, "balance")){
                wantBalance = true;
            }
            if (!wantWithdrawal && !wantDeposit && !wantBalance){
                System.out.println("I do not understand what you are trying to say. Please contact support or try again in simpler terms.");
            }
            if ((wantWithdrawal && wantDeposit)){
                System.out.println("Only one deposit or withdrawal can be completed at a time. Please consider only moving the net amount of money you wish to move, or contact support.");
            }
            amount = detectAmount(sent.tokens());
            if (wantBalance){
                System.out.println("Your balance is $" + balance + ".");
                wantBalance = false;
            }
            if (amount <= 0 && (wantDeposit ^ wantWithdrawal)){
                System.out.print("How much would you like to ");
                if (wantDeposit){
                    System.out.println("deposit?");
                }else if (wantWithdrawal){
                    System.out.println("withdraw?" );
                }
                String additionalinput = scan.nextLine();
                doc = new Document(additionalinput);
                amount = detectAmount(doc.sentence(0).tokens());
            }
            while (amount <= 0 && (wantDeposit ^ wantWithdrawal)){
                System.out.print("Valid number not detected; How much would you like to ");
                if (wantDeposit){
                    System.out.println("deposit? ");
                }else if (wantWithdrawal){
                    System.out.println("withdraw? " );
                }
                input = scan.nextLine();
                doc = new Document(input);
                amount = detectAmount(doc.sentence(0).tokens());
            }
            if (amount > 0 && (wantDeposit ^ wantWithdrawal)){
                System.out.println("");
                System.out.print("Please confirm you are ");
                if (wantDeposit){
                    System.out.print("depositing $");
                }else if (wantWithdrawal){
                    System.out.print("withdrawing $" );
                }
                System.out.println(amount + ": ");
                String additionalinput2 = scan.nextLine();
                doc = new Document(additionalinput2);
                if (yesOrNo(doc)){
                    if (wantWithdrawal){
                        System.out.println("Withdrawing $" + amount + "...");
                        if (balance >= amount){
                            balance -= amount;
                            System.out.println("Please collect cash in the slot below.");
                        }else{
                            System.out.println("Sorry, your request could not be completed as the desired amount would create an overdraft. Please consider a smaller amount to withdraw or contact support.");
                        }
                        System.out.println("Your balance is $" + balance + ".");
                    }
                    if (wantDeposit){
                        System.out.println("Depositing $" + amount + "...");
                        balance += amount;
                        System.out.println("Please insert cash in the slot below.");
                        System.out.println("Your balance is $" + balance + ".");
                    }
                }else{
                    System.out.println("Action cancelled.");
                    System.out.println("Your balance is $" + balance + ".");
                }
            }
        }
        return balance;
    }

    public static boolean yesOrNo(Document doc){
        for (Sentence sent: doc.sentences()){
            List<String> lemmas = sent.lemmas();
            if (containsLemma(lemmas, "yes") || containsLemma(lemmas, "affirmative") || containsLemma(lemmas, "sure") || (containsLemma(lemmas, "why") && containsLemma(lemmas, "not"))){
                return true;
            }
        }
        return false;
    }
}
