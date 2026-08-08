/**
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

window.addEventListener('load', () => {
    document.querySelectorAll('span.copy-icon').forEach(element => {
        element.addEventListener('click', (el) => copyElementsContentToClipboard(element));
    })

    document.querySelectorAll('span.anchor-icon').forEach(element => {
        element.addEventListener('click', (el) => {
            if(element.hasAttribute('pointing-to')){
                const location = hrefWithoutCurrentlyUsedAnchor() + '#' + element.getAttribute('pointing-to')
                copyTextToClipboard(element, location)
            }
        });
    })
})

const copyElementsContentToClipboard = (element) => {
    const selection = window.getSelection();
    const range = document.createRange();
    range.selectNodeContents(element.parentNode.parentNode);
    selection.removeAllRanges();
    selection.addRange(range);

    copyAndShowPopup(element,  () => selection.removeAllRanges())
}

const copyTextToClipboard = (element, text) => {
    var textarea = document.createElement("textarea");
    textarea.textContent = text;
    textarea.style.position = "fixed";
    document.body.appendChild(textarea);
    textarea.select();

    copyAndShowPopup(element, () => document.body.removeChild(textarea))
}

const copyAndShowPopup = (element, after) => {
    try {
        document.execCommand('copy');
        element.nextElementSibling.classList.add('active-popup');
        setTimeout(() => {
            element.nextElementSibling.classList.remove('active-popup');
        }, 1200);
    } catch (e) {
        console.error('Failed to write to clipboard:', e)
    }
    finally {
        if(after) after()
    }
}

const hrefWithoutCurrentlyUsedAnchor = () => window.location.href.split('#')[0]

