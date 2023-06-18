/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 */

using org_noureddine_joularjx_gui.Models;
using org_noureddine_joularjx_gui.Pages;
using org_noureddine_joularjx_gui.Shared;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media.Animation;
using Microsoft.UI.Xaml.Navigation;
using Newtonsoft.Json;
using System;
using System.Runtime.InteropServices;
using System.Threading.Tasks;
using Windows.Storage.Pickers;
using Windows.Storage;
using WinRT;
using Microsoft.UI.Windowing;

namespace org_noureddine_joularjx_gui
{
    /// <summary>
    /// The main window of the application
    /// </summary>
    public sealed partial class MainWindow : Window
    {

        public MainWindow()
        {
            this.InitializeComponent();
            WindowManagement.SetupDefaultSize(this);
            navigationView.ItemInvoked += NavigationView_ItemInvoked;
            navigationView.Loaded += NavigationView_Loaded;
            DisableNavigationItemsExceptHome();

            ExtendsContentIntoTitleBar = true;
            SetTitleBar(appTitleBar);
        }
        /// <summary>
        /// Invoked when the main window page is loaded
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void NavigationView_Loaded(object sender, RoutedEventArgs e)
        {
            //Launch "HomePage" as the default page
            ContentFrame.Navigate(typeof(HomePage));
            navigationView.SelectedItem = homeNavItem;
        }
        /// <summary>
        /// Disables all navigation view items except the home navigation item at the start before the user selects the joular results folder
        /// </summary>
        private void DisableNavigationItemsExceptHome()
        {
            foreach (NavigationViewItemBase item in navigationView.MenuItems)
            {
                if (item is NavigationViewItem navigationItem && navigationItem != homeNavItem)
                {
                    navigationItem.IsEnabled = false;
                }
            }
        }
        /// <summary>
        /// Invoked when a navigation trough the menu has been made
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void ContentFrame_Navigated(object sender, NavigationEventArgs e)
        {
            // Update the "back" button to allow it to go back
            navigationView.IsBackEnabled = ContentFrame.CanGoBack;
        }
        /// <summary>
        /// Invoked when the back button is clicked, it permits to go back to the last page and the last navigation item
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        private void NavigationView_BackRequested(NavigationView sender, NavigationViewBackRequestedEventArgs args)
        {
            Type oldPageType = ContentFrame.CurrentSourcePageType;
            NavigationViewItem oldNavItemSelected = (NavigationViewItem)navigationView.SelectedItem;
            if (ContentFrame.CanGoBack)
            {
                ContentFrame.GoBack();
            }
            Type currentPageType = ContentFrame.CurrentSourcePageType;

            //Update the navigation view item selected according to the last and the current page
            if (oldPageType == typeof(MethodPage))
            {
                switch (oldNavItemSelected.Tag?.ToString())
                {
                    case "MethodsAppPage":
                        NavigateToNavItemPage(methodsAppNavItem);
                        break;
                    case "MethodsAllPage":
                        NavigateToNavItemPage(methodsAllNavItem);
                        break;
                    case "CalltreesAppPage":
                        NavigateToNavItemPage(calltreesAppNavItem);
                        break;
                    case "CalltreesAllPage":
                        NavigateToNavItemPage(calltreesAllNavItem);
                        break;
                    default:
                        break;
                }
            }
            else if (currentPageType == typeof(HomePage))
            {
                navigationView.SelectedItem = homeNavItem;
            }
            else if (currentPageType == typeof(AllPIDsPage))
            {
                navigationView.SelectedItem = allPIDsNavItem;
            }
            else if (currentPageType == typeof(AllMethodsPage))
            {
                navigationView.SelectedItem = methodsAppNavItem;
            }
        }
        /// <summary>
        /// Invoked when a navigation view item has been clicked
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        private void NavigationView_ItemInvoked(NavigationView sender, NavigationViewItemInvokedEventArgs args)
        {
            NavigationViewItem selectedItem = args.InvokedItemContainer as NavigationViewItem;
            if(selectedItem != null)
            {
                NavigateToNavItemPage(selectedItem);
            }
        }
        /// <summary>
        /// Invoked when the selected navigation view item changes
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        private void NavigationView_SelectionChanged(NavigationView sender, NavigationViewSelectionChangedEventArgs args)
        {
            NavigationViewItem selectedNavItem = args.SelectedItem as NavigationViewItem;
            if(selectedNavItem != null)
            {
                NavigateToNavItemPage(selectedNavItem);
            }            
        }
        /// <summary>
        /// Permits to navigate to the convenient page according to a navigation view item 
        /// </summary>
        /// <param name="item">the navigation view item</param>
        private void NavigateToNavItemPage(NavigationViewItem item)
        {
            AllMethodsPageParameters allMethodsPageParameters = new AllMethodsPageParameters();
            string navigationState;
            if (item == null) { return; }
            string tag = item.Tag.ToString();
            if (string.IsNullOrEmpty(tag)) { return; }
            //Navigate to the convenient page according to the navigation view item's tag
            switch (tag)
            {
                case "HomePage":
                    ContentFrame.Navigate(typeof(HomePage));
                    break;
                case "AllPIDsPage":
                    navigationState = JsonConvert.SerializeObject(CurrentJoularResultsItems.PIDs);
                    ContentFrame.Navigate(typeof(AllPIDsPage), navigationState);
                    break;
                case "PIDPage":
                    navigationView.SelectedItem = methodsNavItem;
                    break;
                case "MethodsPage":
                    navigationView.SelectedItem = methodsAppNavItem;
                    break;
                case "MethodsAppPage":
                    allMethodsPageParameters.MethodType = MethodType.Type.Method;
                    allMethodsPageParameters.AppType = AppType.Type.App;
                    navigationState = JsonConvert.SerializeObject(allMethodsPageParameters);
                    ContentFrame.Navigate(typeof(AllMethodsPage), navigationState);
                    break;
                case "MethodsAllPage":
                    allMethodsPageParameters.MethodType = MethodType.Type.Method;
                    allMethodsPageParameters.AppType = AppType.Type.All;
                    navigationState = JsonConvert.SerializeObject(allMethodsPageParameters);
                    ContentFrame.Navigate(typeof(AllMethodsPage), navigationState, new EntranceNavigationTransitionInfo());
                    break;
                case "CalltreesPage":
                    navigationView.SelectedItem = calltreesAppNavItem;
                    break;
                case "CalltreesAppPage":
                    allMethodsPageParameters.MethodType = MethodType.Type.Calltree;
                    allMethodsPageParameters.AppType = AppType.Type.App;
                    navigationState = JsonConvert.SerializeObject(allMethodsPageParameters);
                    ContentFrame.Navigate(typeof(AllMethodsPage), navigationState);
                    break;
                case "CalltreesAllPage":
                    allMethodsPageParameters.MethodType = MethodType.Type.Calltree;
                    allMethodsPageParameters.AppType = AppType.Type.All;
                    navigationState = JsonConvert.SerializeObject(allMethodsPageParameters);
                    ContentFrame.Navigate(typeof(AllMethodsPage), navigationState);
                    break;
                default:
                    break;
            }
        }
        /// <summary>
        /// Shows a folder picker and store the selected folder into "CurrentJoularResultsItems.joularResultsFolderPath"
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        /// <returns></returns>
        public async Task SaveJoularResultsFolderFromFolderPicker(object sender, RoutedEventArgs e)
        {
            // Create a folder picker
            FolderPicker openPicker = new FolderPicker();
            // Retrieve the window handle (HWND) of the current WinUI 3 window.
            var hWnd = WinRT.Interop.WindowNative.GetWindowHandle(this);
            // Initialize the folder picker with the window handle (HWND).
            WinRT.Interop.InitializeWithWindow.Initialize(openPicker, hWnd);
            // Set options for your folder picker
            openPicker.SuggestedStartLocation = PickerLocationId.Desktop;
            openPicker.FileTypeFilter.Add("*");
            // Open the picker for the user to pick a folder
            StorageFolder folder = await openPicker.PickSingleFolderAsync();
            if (folder != null)
            {
                CurrentJoularResultsItems.joularResultsFolderPath = folder.Path;
            }
        }
        /// <summary>
        /// The interface to show the selecting folder dialog box to the user
        /// </summary>
        [ComImport]
        [Guid("3E68D4BD-7135-4D10-8018-9FB6D9F33FA1")]
        [InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        public interface IInitializeWithWindow
        {
            void Initialize(IntPtr hwnd);
        }
        [ComImport]
        [InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        [Guid("EECDBF0E-BAE9-4CB6-A68E-9598E1CB57BB")]
        internal interface IWindowNative
        {
            IntPtr WindowHandle { get; }
        }
    }
}