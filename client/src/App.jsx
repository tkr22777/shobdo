import React, {Component} from 'react';
import SearchBar from './components/SearchBar.jsx';
import SearchSuggestions from './components/SearchSuggestions.jsx';
import './App.css';
import _ from 'lodash';
import axios from 'axios';

class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            searchSuggestions: []
        };

    }
    searchForSuggestions(searchTerm) {
        axios.post('http://138.68.224.203:9000/dict/search', {
            spelling: searchTerm
        }).then((response) => {
            console.info(response);
            this.setState({
                searchSuggestions: response.data
            });
        }).catch((error) => {
            // eslint-disable-next-line
            console.log(error);
        });
    }

    render() {
        const search = _.debounce((searchTerm) => {
            this.searchForSuggestions(searchTerm);
        }, 300);


        return (
            <div>
                <h1>Search</h1>
                <SearchBar onSearchTermChange={searchTerm => search(searchTerm)} />
                <SearchSuggestions suggestions={this.state.searchSuggestions} />
            </div>

        );
    }
}

App.displayName = 'ShobdoApp';
export default App;
