/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 */

using Microsoft.UI.Windowing;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Media;
using System.Linq.Expressions;

namespace org_noureddine_joularjx_gui.Shared
{
    public static class WindowManagement
    {
        /// <summary>
        /// The default width of the app while launching
        /// </summary>
        private static int _defaultWidth = 1152;
        /// <summary>
        /// The default height of the app while launching
        /// </summary>
        private static int _defaultHeight = 864;
        /// <summary>
        /// Setup the defaults size to the application
        /// </summary>
        /// <param name="window"></param>
        public static void SetupDefaultSize(Window window)
        {
            AppWindow appWindow = WindowManagement.GetCurrentAppWindow(window);
            WindowManagement.ResizeAppWindow(appWindow, _defaultWidth, _defaultHeight);
        }
        /// <summary>
        /// Centers the app's window
        /// </summary>
        /// <param name="window"></param>
        public static void CenterWindow(Window window)
        {
            AppWindow appWindow = GetCurrentAppWindow(window);
            DisplayArea displayArea = GetDisplayArea(window);
            if (appWindow is not null)
            {
                if (displayArea is not null)
                {
                    var CenteredPosition = appWindow.Position;
                    CenteredPosition.X = ((displayArea.WorkArea.Width - appWindow.Size.Width) / 2);
                    CenteredPosition.Y = ((displayArea.WorkArea.Height - appWindow.Size.Height) / 2);
                    appWindow.Move(CenteredPosition);
                }
            }
        }
        public static AppWindow GetCurrentAppWindow(Window window)
        {
            var hWnd = WinRT.Interop.WindowNative.GetWindowHandle(window);
            Microsoft.UI.WindowId windowId = Microsoft.UI.Win32Interop.GetWindowIdFromWindow(hWnd);
            AppWindow appWindow = AppWindow.GetFromWindowId(windowId);
            return appWindow;
        }

        private static DisplayArea GetDisplayArea(Window window)
        {
            var hWnd = WinRT.Interop.WindowNative.GetWindowHandle(window);
            Microsoft.UI.WindowId windowId = Microsoft.UI.Win32Interop.GetWindowIdFromWindow(hWnd);
            DisplayArea displayArea = DisplayArea.GetFromWindowId(windowId, DisplayAreaFallback.Nearest);
            return displayArea;
        }

        public static void ResizeAppWindow(AppWindow appWindow, int width, int height)
        {
            appWindow.Resize(new Windows.Graphics.SizeInt32 { Width = width, Height = height });
        }

        public static void ExpandWindow(Window window)
        {
            AppWindow appWindow = GetCurrentAppWindow(window);
            DisplayArea displayArea = GetDisplayArea(window);
            var fullPosition = appWindow.Position;
            fullPosition.X = displayArea.WorkArea.Width;
            fullPosition.Y = displayArea.WorkArea.Height;
            ResizeAppWindow(appWindow, fullPosition.X, fullPosition.Y);
        }

        public static T FindFirstParentOfType<T>(this DependencyObject child) where T : DependencyObject
        {
            DependencyObject parent = VisualTreeHelper.GetParent(child);

            if (parent == null)
                return null;

            if (parent is T)
                return (T)parent;

            return FindFirstParentOfType<T>(parent);
        }
    }
}
