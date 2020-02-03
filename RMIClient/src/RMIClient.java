import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

public class RMIClient {

    public static void main(String[] args) {
        String rmiAddress = "rmi://localhost:2000/frontend";
        try {
            FrontEndInterface frontend = (FrontEndInterface) Naming.lookup(rmiAddress);
            mainMenu(frontend);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static void mainMenu(FrontEndInterface frontend) {
        boolean quit = false;
        Scanner scanner = new Scanner(System.in);

        while (!quit) {
            System.out.println("\nMENU\n");
            System.out.println("1 - Adicionar Place");
            System.out.println("2 - Procurar Place ");
            System.out.println("3 - Listar Places");
            System.out.println("4 - Adicionar PlaceManager");
            System.out.println("5 - Mostrar Lider");
            System.out.println("6 - Remover Place");
            System.out.println("0 - Quit");
            System.out.print("Escolha a opcao: ");

            int option = scanner.nextInt();
            scanner.nextLine(); //limpar o buffer

            switch (option) {
                case 0 : {
                    quit = true;
                    break;
                }
                case 1 : {
                    System.out.println("\nCodigo-Postal:");
                    String postalCode = scanner.nextLine();
                    System.out.println("Localidade:");
                    String locality = scanner.nextLine();
                    try {
                        boolean placeAdded = frontend.addPlace(new Place(postalCode, locality));
                        if (placeAdded) {
                            System.out.println("\nPlace " + postalCode + " - " + locality + " Adicionado com sucesso!");
                        } else {
                            System.out.println("\nPlace ja existe!");
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                case 2 : {
                    System.out.println("\nCodigo-Postal:");
                    String postalCode = scanner.nextLine();

                    try {
                        Place place = frontend.getPlace(postalCode);
                        if (place == null) {
                            System.out.println("\nPlace nao existe");
                        } else {
                            System.out.println("\n" + place);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                case 3 : {
                    try {
                        ArrayList<Place> places = frontend.allPlaces();
                        System.out.println("\nLista De Places:\n");
                        for (Place p: places) {
                            System.out.println(p);
                        }

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                }

                case 4 : {
                    System.out.println("\nPlaceManager port:");
                    int port = scanner.nextInt();
                    try {
                        frontend.addNode(port);
                        System.out.println("\nPlaceManager iniciado!");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case 5 : {
                    try {
                        System.out.println("Lider " + frontend.getLeader());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                case 6 : {
                    System.out.println("\nCodigo-Postal:");
                    String postalCode = scanner.nextLine();
                    try {
                        Place place = frontend.getPlace(postalCode);
                        if (place == null) {
                            System.out.println("\nPlaceManager nao existe");
                        } else {
                            System.out.println("\n" + place + " para remover.");
                            boolean placeRemoved = frontend.removePlace(place);
                            if (placeRemoved) {
                                System.out.println("\nPlace " + postalCode + " removido com sucesso!");
                            } else {
                                System.out.println("\nPlace nao existe!");
                            }
                        }

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;

                }


            }
        }



    }
}
