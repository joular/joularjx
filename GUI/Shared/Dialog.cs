/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 */

using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using System;
using System.Runtime.InteropServices;
using Windows.UI.Popups;

namespace org_noureddine_joularjx_gui.Shared
{
    /// <summary>
    /// Permit to show a dialog box 
    /// </summary>
    public static class Dialog
    {
        /// <summary>
        /// The methods that allows to show the dialog box
        /// </summary>
        /// <param name="element">The element from wich it has been called (Ex: a button)</param>
        /// <param name="message"></param>
        /// <param name="title"></param>
        public static async void ShowMessage(this FrameworkElement element, string message, string title = "Message")
        {
            ContentDialog dialog = new ContentDialog()
            {
                Title = title,
                Content = message,
                XamlRoot = element.XamlRoot,
                CloseButtonText = "OK"
            };
            ContentDialogResult result = await dialog.ShowAsync();
        }
    }
}
